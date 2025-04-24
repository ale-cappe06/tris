package gui;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import model.Move;
import network.GameState;
import model.PlayerSymbol;
import database.DatabaseManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GameGUI extends JFrame {
    private static final Logger logger = LogManager.getLogger(GameGUI.class);

    // Griglia dei pulsanti che rappresentano il tabellone 3x3
    private JButton[][] buttons = new JButton[3][3];

    // Simbolo del giocatore (X o O)
    private PlayerSymbol playerSymbol;

    // Stream per la comunicazione col server
    private ObjectOutputStream out;
    private ObjectInputStream in;

    // Nome utente loggato
    private String username;

    // Riferimento al menu principale per tornare dopo la partita
    private JFrame parentMenu;

    // Costruttore principale: inizializza la GUI e la connessione
    public GameGUI(Socket socket, String username, JFrame parentMenu) throws IOException {
        this.username = username;
        this.parentMenu = parentMenu;

        setTitle("Tris Multiplayer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Pannello del tabellone
        JPanel boardPanel = new JPanel(new GridLayout(3, 3));
        boardPanel.setPreferredSize(new Dimension(300, 300)); // dimensione predefinita

        // Inizializza gli stream di comunicazione col server
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());

        // Inizializzazione dei pulsanti della griglia
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++) {
                int finalI = i, finalJ = j;
                buttons[i][j] = new JButton("-");
                buttons[i][j].setFont(new Font(Font.SANS_SERIF, Font.BOLD, 40));
                buttons[i][j].setFocusPainted(false);
                buttons[i][j].setPreferredSize(new Dimension(100, 100)); // dimensione fissa
                buttons[i][j].addActionListener(e -> sendMove(finalI, finalJ)); // azione al click
                boardPanel.add(buttons[i][j]);
            }

        add(boardPanel, BorderLayout.CENTER);

        pack(); // adatta le dimensioni della finestra
        setLocationRelativeTo(null); // centra la finestra sullo schermo
        setVisible(true); // rende visibile la finestra

        // Thread secondario per ricevere dati dal server
        new Thread(this::listenToServer).start();
    }

    // Invia una mossa al server
    private void sendMove(int row, int col) {
        try {
            logger.info("Invio mossa: ({}, {}) per il giocatore {}", row, col, playerSymbol);
            out.writeObject(new Move(row, col, playerSymbol));
            out.flush();
        } catch (IOException e) {
            logger.error("Errore di invio mossa: {}", e.getMessage());
            JOptionPane.showMessageDialog(this, "Errore di invio mossa");
        }
    }

    // Riceve aggiornamenti dal server in un loop
    private void listenToServer() {
        try {
            logger.info("In ascolto dal server...");
            while (true) {
                GameState state = (GameState) in.readObject(); // riceve lo stato della partita

                // Mostra un messaggio se si è in attesa del secondo giocatore
                if (state.message.contains("Aspetta che si unisca un altro giocatore")) {
                    setTitle("Attendi il secondo giocatore...");
                    logger.info("In attesa del secondo giocatore...");
                } else {
                    playerSymbol = state.currentPlayer;
                    logger.info("Giocatore corrente: {}", playerSymbol);
                    updateBoard(state); // aggiorna la griglia con lo stato ricevuto
                }

                // Se la partita è finita, mostra il risultato
                if (state.gameOver) {
                    logger.info("Partita terminata: {}", state.message);
                    JOptionPane.showMessageDialog(this, state.message);

                    // Aggiorna le statistiche nel database in base al risultato
                    if (state.message.contains("ha vinto")) {
                        if (state.message.contains(playerSymbol.toString())) {
                            DatabaseManager.incrementVittorie(username);
                            logger.info("Incrementata vittoria per {}", username);
                        } else {
                            DatabaseManager.incrementSconfitte(username);
                            logger.info("Incrementata sconfitta per {}", username);
                        }
                    } else if (state.message.contains("Pareggio")) {
                        DatabaseManager.incrementPareggi(username);
                        logger.info("Incrementato pareggio per {}", username);
                    }

                    // Chiude la finestra e ritorna al menu
                    this.dispose();
                    parentMenu.setVisible(true);
                    logger.info("Ritorno al menu principale.");
                    return;
                }
            }
        } catch (Exception e) {
            logger.error("Connessione persa col server: {}", e.getMessage());
            JOptionPane.showMessageDialog(this, "Connessione persa col server");

            // Chiude gli stream e termina l'applicazione
            try {
                if (out != null) out.close();
                if (in != null) in.close();
            } catch (IOException ignored) {}

            System.exit(1);
        }
    }

    // Aggiorna la griglia del tabellone con i dati ricevuti
    private void updateBoard(GameState state) {
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                buttons[i][j].setText(Character.toString(state.board[i][j]));
        setTitle(state.message); // mostra un messaggio nella barra del titolo
        logger.debug("Tabellone aggiornato: \n{}", formatBoard(state));
    }

    // Ritorna una rappresentazione testuale del tabellone per il log
    private String formatBoard(GameState state) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                sb.append(state.board[i][j]).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
