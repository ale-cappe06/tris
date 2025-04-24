package network;

import java.io.*;  // Importa le classi per gestire le operazioni di I/O
import javax.net.ssl.SSLSocket;
import java.net.*;  // Importa le classi per gestire la connessione di rete
import java.util.concurrent.CountDownLatch;  // Importa CountDownLatch per sincronizzare l'avvio del gioco
import model.PlayerSymbol;  // Importa la classe PlayerSymbol che definisce i simboli dei giocatori (X, O)
import model.Move;  // Importa la classe Move che rappresenta una mossa nel gioco
import org.apache.logging.log4j.LogManager;  // Importa il logger
import org.apache.logging.log4j.Logger;  // Importa il logger

// Classe che gestisce la connessione con un client (un giocatore) durante la partita
public class ClientHandler implements Runnable {
    private static final Logger logger = LogManager.getLogger(ClientHandler.class);  // Logger per il tracking delle attività

    private SSLSocket socket;  // Socket per la connessione con il client
    private PlayerSymbol symbol;  // Simbolo del giocatore (X o O)
    private GameState gameState;  // Stato del gioco (griglia, messaggi, ecc.)
    private ObjectOutputStream out;  // Stream di output per inviare dati al client
    private ObjectInputStream in;  // Stream di input per ricevere dati dal client
    private ClientHandler[] players;  // Array di giocatori connessi (due giocatori)
    private CountDownLatch startLatch;  // Latch per sincronizzare l'avvio del gioco

    // Costruttore che inizializza i vari componenti
    public ClientHandler(SSLSocket socket, PlayerSymbol symbol, GameState state, ClientHandler[] players, CountDownLatch latch) {
        this.socket = socket;  // Imposta la connessione socket
        this.symbol = symbol;  // Imposta il simbolo del giocatore (X o O)
        this.gameState = state;  // Imposta lo stato del gioco
        this.players = players;  // Imposta l'array di giocatori
        this.startLatch = latch;  // Imposta il latch per la sincronizzazione
    }

    // Metodo che gestisce l'esecuzione della logica del gioco per questo giocatore
    @Override
    public void run() {
        try {
            // Inizializza gli stream di input e output per la comunicazione con il client
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            logger.info("Connessione stabilita con il giocatore: " + symbol);  // Log della connessione

            // Invia un messaggio di stato al giocatore per informarlo che deve aspettare un altro giocatore
            sendGameState("Sei il giocatore " + symbol + ". Aspetta che si unisca un altro giocatore...");

            // Attende che entrambi i giocatori siano pronti
            startLatch.await();
            sendGameState("La partita è iniziata!");  // Invia un messaggio che la partita è iniziata
            logger.info("La partita è iniziata!");  // Log dell'inizio partita

            // Ciclo di gioco fino alla fine della partita
            while (!gameState.gameOver) {
                Move move = (Move) in.readObject();  // Legge la mossa inviata dal giocatore
                logger.info("Giocatore " + symbol + " ha inviato una mossa: riga " + move.row + ", colonna " + move.col);

                // Sincronizza l'accesso alla variabile shared gameState per evitare conflitti tra i thread
                synchronized (gameState) {
                    // Verifica che il giocatore sia il giocatore corrente e che la cella non sia già occupata
                    if (symbol == gameState.currentPlayer && gameState.board[move.row][move.col] == '-') {
                        // Aggiorna lo stato del gioco con la mossa del giocatore
                        gameState.board[move.row][move.col] = symbol.name().charAt(0);

                        // Controlla se il giocatore ha vinto
                        if (checkWin(symbol)) {
                            gameState.message = "Il giocatore " + symbol + " ha vinto!";  // Imposta il messaggio di vittoria
                            gameState.gameOver = true;  // Termina la partita
                            logger.info("Il giocatore " + symbol + " ha vinto!");  // Log della vittoria
                        }
                        // Controlla se c'è un pareggio
                        else if (isDraw()) {
                            gameState.message = "Pareggio!";  // Imposta il messaggio di pareggio
                            gameState.gameOver = true;  // Termina la partita
                            logger.info("La partita è finita con un pareggio.");  // Log del pareggio
                        }
                        // Se la partita non è finita, passa il turno al giocatore successivo
                        else {
                            gameState.currentPlayer = (symbol == PlayerSymbol.X) ? PlayerSymbol.O : PlayerSymbol.X;
                            gameState.message = "Turno del giocatore " + gameState.currentPlayer;
                            logger.info("Il turno passa al giocatore " + gameState.currentPlayer);
                        }

                        // Invia lo stato del gioco aggiornato a tutti i giocatori
                        broadcast();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Connessione persa con il giocatore " + symbol + ": " + e.getMessage());  // Log degli errori di connessione
        } finally {
            try { if (out != null) out.close(); } catch (IOException ignored) {}
            try { if (in != null) in.close(); } catch (IOException ignored) {}
            try { if (socket != null && !socket.isClosed()) socket.close(); } catch (IOException ignored) {}
            logger.info("Connessione chiusa per il giocatore " + symbol);  // Log della chiusura della connessione
        }
    }

    // Metodo che verifica se il giocatore ha vinto
    private boolean checkWin(PlayerSymbol s) {
        char sym = s.name().charAt(0);  // Ottiene il simbolo del giocatore
        char[][] b = gameState.board;  // Ottiene la griglia del gioco

        // Controlla le righe, le colonne e le diagonali per una vittoria
        for (int i = 0; i < 3; i++)
            if (b[i][0] == sym && b[i][1] == sym && b[i][2] == sym) return true;
        for (int j = 0; j < 3; j++)
            if (b[0][j] == sym && b[1][j] == sym && b[2][j] == sym) return true;
        return (b[0][0] == sym && b[1][1] == sym && b[2][2] == sym)
            || (b[0][2] == sym && b[1][1] == sym && b[2][0] == sym);
    }

    // Metodo che verifica se la partita è finita con un pareggio
    private boolean isDraw() {
        // Verifica se ci sono ancora spazi vuoti ('-') nella griglia
        for (char[] row : gameState.board)
            for (char c : row)
                if (c == '-') return false;
        return true;  // Se non ci sono spazi vuoti, è un pareggio
    }

    // Metodo per inviare lo stato del gioco a tutti i giocatori
    private void broadcast() throws IOException {
        for (ClientHandler player : players)
            if (player != null) player.sendGameState(gameState.message);  // Invia lo stato a ogni giocatore
    }

    // Metodo per inviare lo stato del gioco a questo giocatore
    private void sendGameState(String msg) throws IOException {
        gameState.message = msg;  // Imposta il messaggio
        out.reset();  // Resetta lo stream per evitare problemi di cache
        out.writeObject(gameState);  // Scrive l'oggetto GameState
        out.flush();  // Assicura che i dati siano inviati
        logger.info("Stato del gioco inviato ai giocatori: " + msg);  // Log dello stato inviato
    }
}
