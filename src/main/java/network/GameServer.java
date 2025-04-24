package network;

import java.io.*;  // Importa le classi per la gestione delle operazioni di I/O
import java.net.*;  // Importa le classi per la gestione delle connessioni di rete
import java.util.concurrent.*;  // Importa le classi per la gestione dei thread concorrenti
import model.PlayerSymbol;  // Importa la classe PlayerSymbol che definisce i simboli dei giocatori (X, O)
import org.apache.logging.log4j.LogManager;  // Importa la libreria per il logging
import org.apache.logging.log4j.Logger;  // Importa la classe Logger per la registrazione dei log

import javax.net.ssl.*;  // Importa le classi per la gestione di connessioni SSL
import java.security.KeyStore;  // Importa la classe per gestire il keystore

// Classe che gestisce la logica del server per una partita di Tic-Tac-Toe
public class GameServer {
    private static final Logger logger = LogManager.getLogger(GameServer.class);  // Logger per tracciare gli eventi nel server

    private static final int PORT = 12345;  // Porta su cui il server ascolta le connessioni
    private static final ExecutorService pool = Executors.newCachedThreadPool();  // Pool di thread per gestire le connessioni in modo concorrente (ogni client in un thread separato)
    private static SSLServerSocket serverSocket;  // SSLServerSocket per gestire la connessione sicura con i client

    // Metodo principale che avvia il server
    public static void main(String[] args) {
        try {
            // Caricamento del keystore SSL
            String keystorePath = "server-keystore.jks";  // Percorso del keystore
            String keystorePassword = "ittsvolterra";  // Password del keystore
            char[] password = keystorePassword.toCharArray();

            // Inizializza il keystore
            KeyStore keystore = KeyStore.getInstance("JKS");
            try (FileInputStream keystoreFile = new FileInputStream(keystorePath)) {
                keystore.load(keystoreFile, password);
            }

            // Crea un KeyManagerFactory
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keystore, password);

            // Crea un SSLContext con il KeyManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

            // Crea un SSLServerSocketFactory per ottenere il server socket SSL
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();

            // Avvia il server SSL sulla porta specificata
            serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(PORT);
            logger.info("Server SSL in ascolto sulla porta " + PORT);

            // Aggiunge un hook per la chiusura del server quando il programma viene terminato
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Chiusura del server in corso...");
                try {
                    if (serverSocket != null && !serverSocket.isClosed()) {
                        serverSocket.close();  // Chiude il ServerSocket
                        logger.info("ServerSocket chiuso correttamente.");
                    }
                } catch (IOException e) {
                    logger.error("Errore durante la chiusura del ServerSocket: " + e.getMessage());
                }

                pool.shutdownNow();  // Termina immediatamente il pool di thread
                logger.info("Thread pool terminato.");
            }));

            // Ciclo infinito per gestire le connessioni multiple
            while (true) {
                // Crea un nuovo stato di gioco per ogni partita
                GameState gameState = new GameState();
                CountDownLatch startLatch = new CountDownLatch(1);  // Latch per sincronizzare l'inizio del gioco
                ClientHandler[] players = new ClientHandler[2];  // Array per memorizzare i due giocatori

                // Accetta le connessioni dei due giocatori
                for (int i = 0; i < 2; i++) {
                    SSLSocket clientSocket = (SSLSocket) serverSocket.accept();  // Accetta una connessione SSL in entrata
                    PlayerSymbol symbol = (i == 0) ? PlayerSymbol.X : PlayerSymbol.O;  // Assegna un simbolo al giocatore (X o O)

                    // Log dell'evento di connessione di un nuovo client
                    logger.info("Nuovo client connesso (SSL). Assegnato simbolo: " + symbol);

                    // Crea un ClientHandler per il nuovo giocatore e lo avvia in un thread separato
                    ClientHandler handler = new ClientHandler(clientSocket, symbol, gameState, players, startLatch);
                    players[i] = handler;
                    pool.execute(handler);  // Esegue il handler nel thread pool
                }

                // Una volta che entrambi i giocatori si sono connessi, avvia la partita
                logger.info("Entrambi i giocatori connessi. Inizio partita.");
                startLatch.countDown();  // Decrementa il latch per segnare che il gioco può iniziare
            }

        } catch (Exception e) {
            logger.error("Errore nel server SSL: " + e.getMessage(), e);  // Log dell'errore in caso di problemi
        }
    }
}