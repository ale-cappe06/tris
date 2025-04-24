package gui;

import javax.swing.*;
import java.awt.*;
import javax.net.ssl.*;
import java.security.KeyStore;
import java.io.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import model.UserStats;              // Classe che contiene le statistiche dell'utente
import database.DatabaseManager;     // Gestione delle statistiche e accesso DB

public class PostLoginMenuFrame extends JFrame {

    private static final Logger logger = LogManager.getLogger(PostLoginMenuFrame.class); // Logger per il tracciamento delle azioni

    private JButton singlePlayerButton; // Pulsante per la modalità single player
    private JButton multiPlayerButton;  // Pulsante per la modalità multiplayer
    private JButton statsButton;        // Pulsante per visualizzare le statistiche
    private JButton logoutButton;       // Pulsante per fare il logout
    private String username;            // Nome utente per personalizzare l'interfaccia

    // Costruttore della finestra che viene mostrata dopo il login
    public PostLoginMenuFrame(String username) {
        this.username = username;
        setTitle("Seleziona modalità di gioco"); // Titolo della finestra
        setSize(350, 300); // Imposta le dimensioni della finestra
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Imposta la chiusura dell'applicazione alla chiusura della finestra
        setLocationRelativeTo(null); // Centra la finestra sullo schermo
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS)); // Layout verticale per i pulsanti

        // Etichetta per il titolo
        JLabel titleLabel = new JLabel("Scegli la modalità di gioco");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Allinea l'etichetta al centro
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16)); // Imposta il font per l'etichetta
        add(Box.createVerticalStrut(20)); // Spazio verticale tra l'etichetta e i pulsanti
        add(titleLabel);

        // Inizializzazione dei pulsanti
        singlePlayerButton = new JButton("Gioca contro il Bot");
        multiPlayerButton = new JButton("Gioca contro un altro giocatore");
        statsButton = new JButton("Visualizza le statistiche");
        logoutButton = new JButton("Logout");

        // Allineamento e dimensionamento dei pulsanti
        singlePlayerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        multiPlayerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Aggiungi i pulsanti alla finestra con spaziatura
        add(Box.createVerticalStrut(20));
        add(singlePlayerButton);
        add(Box.createVerticalStrut(10));
        add(multiPlayerButton);
        add(Box.createVerticalStrut(10));
        add(statsButton);
        add(Box.createVerticalStrut(10));
        add(logoutButton);

        // Associa le azioni ai pulsanti
        singlePlayerButton.addActionListener(e -> startSinglePlayerGame()); // Inizia la modalità single player
        multiPlayerButton.addActionListener(e -> startMultiPlayerGame()); // Inizia la modalità multiplayer
        statsButton.addActionListener(e -> showStats()); // Mostra le statistiche
        logoutButton.addActionListener(e -> logout()); // Effettua il logout
    }

    // Metodo per avviare il gioco in modalità single player
    private void startSinglePlayerGame() {
        logger.info("L'utente " + username + " ha scelto di giocare contro il bot.");
        
        // Mostra una finestra di dialogo per selezionare la difficoltà del bot
        String[] options = {"Facile", "Difficile"};
        int scelta = JOptionPane.showOptionDialog(this,
                "Seleziona la difficoltà:",
                "Scegli difficoltà",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]); // Imposta "Facile" come opzione predefinita

        // Avvia il gioco con la difficoltà selezionata
        if (scelta == 0) {
            new SinglePlayerGame(username, this, "facile").setVisible(true); // Modalità facile
            this.setVisible(false); // Nasconde la finestra corrente
        } else if (scelta == 1) {
            new SinglePlayerGame(username, this, "difficile").setVisible(true); // Modalità difficile
            this.setVisible(false); // Nasconde la finestra corrente
        }
    }

    // Metodo per avviare il gioco in modalità multiplayer con SSL
    private void startMultiPlayerGame() {
        logger.info("L'utente " + username + " ha scelto di giocare contro un altro giocatore.");
        
        try {
        	// Percorso del keystore e truststore
        	String keystorePath = "server-keystore.jks";
        	String truststorePath = "client-truststore.jks";
        	String password = "ittsvolterra";
        	char[] passArray = password.toCharArray();

        	// Carica il keystore per autenticazione del client (facoltativo, puoi saltare se non serve mTLS)
        	KeyStore keystore = KeyStore.getInstance("JKS");
        	try (FileInputStream fis = new FileInputStream(keystorePath)) {
        	    keystore.load(fis, passArray);
        	}
        	KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        	kmf.init(keystore, passArray);

        	// Carica il truststore per validare il certificato del server
        	KeyStore truststore = KeyStore.getInstance("JKS");
        	try (FileInputStream fis = new FileInputStream(truststorePath)) {
        	    truststore.load(fis, passArray);
        	}
        	TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        	tmf.init(truststore);

        	// Inizializza il contesto SSL con truststore e keystore
        	SSLContext sslContext = SSLContext.getInstance("TLS");
        	sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        	// Crea il socket
        	SSLSocketFactory socketFactory = sslContext.getSocketFactory();
        	SSLSocket socket = (SSLSocket) socketFactory.createSocket("localhost", 12345);
        	socket.setEnabledProtocols(new String[]{"TLSv1.2"});

            // Avvia il gioco multiplayer passando il socket SSL
            new GameGUI(socket, username, this).setVisible(true);
            this.setVisible(false); // Nasconde la finestra corrente

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Impossibile connettersi al server multiplayer SSL."); // Gestione errore di connessione
            logger.error("Errore nella connessione SSL al server multiplayer: " + e.getMessage()); // Log dell'errore
        }
    }

    // Metodo per visualizzare le statistiche dell'utente
    private void showStats() {
        logger.info("L'utente " + username + " ha richiesto di visualizzare le proprie statistiche.");
        
        // Recupera le statistiche dall'oggetto UserStats
        UserStats stats = DatabaseManager.getUserStats(username);
        if (stats != null) {
            // Mostra le statistiche in una finestra di dialogo
            JOptionPane.showMessageDialog(this,
                    "Statistiche di " + username + ":\n" +
                            "Vittorie: " + stats.getVittorie() + "\n" +
                            "Sconfitte: " + stats.getSconfitte() + "\n" +
                            "Pareggi: " + stats.getPareggi(),
                    "Statistiche", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Impossibile recuperare le statistiche."); // Gestione errore nel recupero delle statistiche
            logger.error("Impossibile recuperare le statistiche per l'utente " + username); // Log dell'errore
        }
    }

    // Metodo per effettuare il logout dell'utente
    private void logout() {
        logger.info("L'utente " + username + " ha scelto di effettuare il logout.");
        
        // Finestra di conferma per il logout
        int option = JOptionPane.showConfirmDialog(this,
                "Sei sicuro di voler effettuare il logout?", "Logout",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        // Se l'utente conferma, esegui il logout
        if (option == JOptionPane.YES_OPTION) {
            this.setVisible(false); // Nasconde la finestra corrente
            new LoginFrame().setVisible(true); // Mostra la finestra di login
        }
    }
}
