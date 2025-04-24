package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import database.DatabaseManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Classe che gestisce la logica di gioco per la modalità single player
public class SinglePlayerGame extends JFrame {
    private static final Logger logger = LogManager.getLogger(SinglePlayerGame.class);  // Logger per il tracking delle operazioni

    // Array di bottoni per rappresentare la griglia di gioco 3x3
    private JButton[] buttons = new JButton[9];
    private char[][] board = new char[3][3];  // La griglia di gioco 3x3
    private char currentPlayer = 'X';  // Giocatore iniziale (X)
    private int playerWins = 0, botWins = 0, draws = 0;  // Contatori per vittorie, sconfitte e pareggi
    private boolean gameOver = false;  // Flag per indicare se il gioco è finito
    private String username;  // Nome utente del giocatore
    private String difficulty;  // Difficoltà selezionata (facile o difficile)
    private JLabel scoreLabel;  // Etichetta per visualizzare il punteggio
    private JButton newGameButton;  // Bottone per iniziare una nuova partita
    private JButton menuButton;  // Bottone per tornare al menu principale
    private int totalMoves = 0;  // Numero totale di mosse effettuate durante la partita
    private List<String> moveHistory = new ArrayList<>();  // Storia delle mosse della partita

    // Costruttore della classe che inizializza la finestra di gioco
    public SinglePlayerGame(String username, JFrame parentFrame, String difficulty) {
        this.username = username;
        this.difficulty = difficulty;
        logger.info("Avvio partita single player. Utente: {}, Difficoltà: {}", username, difficulty);

        setTitle("Gioco Tris - Modalità Single Player");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 450);
        setLocationRelativeTo(null); // Centra la finestra sullo schermo
        setLayout(new BorderLayout());  // Layout per la finestra

        // Pannello per la griglia di gioco (3x3)
        JPanel boardPanel = new JPanel(new GridLayout(3, 3));
        boardPanel.setPreferredSize(new Dimension(300, 300)); // Imposta dimensione iniziale corretta
        for (int i = 0; i < 9; i++) {
            buttons[i] = new JButton();
            buttons[i].setFont(new Font("Arial", Font.PLAIN, 60));  // Imposta il font per i bottoni
            buttons[i].setFocusPainted(false);
            buttons[i].setPreferredSize(new Dimension(100, 100));  // Forza dimensione coerente dei bottoni
            buttons[i].addActionListener(this::handlePlayerMove);  // Gestisce la mossa del giocatore
            boardPanel.add(buttons[i]);
        }

        // Etichetta per visualizzare il punteggio
        scoreLabel = new JLabel("Vittorie: " + playerWins + " - Sconfitte: " + botWins + " - Pareggi: " + draws);
        scoreLabel.setHorizontalAlignment(JLabel.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        // Bottone per una nuova partita
        newGameButton = new JButton("Nuova Partita");
        newGameButton.addActionListener(e -> startNewGame());  // Avvia una nuova partita
        newGameButton.setEnabled(false);  // Inizialmente disabilitato

        // Bottone per tornare al menu principale
        menuButton = new JButton("Torna al Menu Principale");
        menuButton.addActionListener(e -> {
            parentFrame.setVisible(true);  // Rende visibile il menu principale
            this.setVisible(false);  // Nasconde la finestra del gioco
        });
        menuButton.setEnabled(false);  // Inizialmente disabilitato

        // Pannello per i bottoni (nuova partita e menu)
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(newGameButton);
        buttonPanel.add(menuButton);

        // Aggiunge i componenti alla finestra
        add(scoreLabel, BorderLayout.NORTH);
        add(boardPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();  // Adatta la finestra ai componenti
        setVisible(true);

        resetBoard();  // Resetta la griglia di gioco per la nuova partita
    }

    // Resetta la griglia e le variabili per una nuova partita
    private void resetBoard() {
        for (int i = 0; i < 9; i++) {
            board[i / 3][i % 3] = ' ';  // Inizializza tutte le celle della griglia come vuote
            buttons[i].setText("");  // Pulisce il testo dei bottoni
            buttons[i].setEnabled(true);  // Rende i bottoni abilitati
            buttons[i].setBackground(null);  // Rimuove il colore di sfondo
        }
        gameOver = false;  // Partita non finita
        currentPlayer = 'X';  // Inizia con il giocatore X
        totalMoves = 0;  // Reset del numero di mosse
        moveHistory.clear();  // Pulisce la cronologia delle mosse
        logger.info("Board resettata per nuova partita.");
    }

    // Gestisce la mossa del giocatore
    private void handlePlayerMove(ActionEvent e) {
        if (gameOver) return;  // Se il gioco è finito, non accetta altre mosse

        JButton clickedButton = (JButton) e.getSource();
        int index = -1;

        // Trova la posizione del bottone cliccato
        for (int i = 0; i < 9; i++) {
            if (buttons[i] == clickedButton) {
                index = i;
                break;
            }
        }

        int row = index / 3;
        int col = index % 3;

        // Se la cella è vuota, il giocatore può fare la mossa
        if (board[row][col] == ' ') {
            board[row][col] = currentPlayer;  // Aggiorna la griglia con la mossa del giocatore
            clickedButton.setText(String.valueOf(currentPlayer));  // Imposta il simbolo sul bottone
            clickedButton.setEnabled(false);  // Disabilita il bottone
            totalMoves++;
            moveHistory.add("Giocatore: X in posizione (" + row + "," + col + ")");
            logger.info("Mossa del giocatore X: ({}, {})", row, col);

            if (checkWin()) {  // Se il giocatore ha vinto
                gameOver = true;
                playerWins++;
                highlightWinningCells('X');  // Evidenzia le celle vincenti
                JOptionPane.showMessageDialog(this, "Complimenti! Hai vinto!");
                DatabaseManager.incrementVittorie(username);  // Incrementa le vittorie nel database
                updateScore();  // Aggiorna il punteggio
                showMoveSummary();  // Mostra il riepilogo della partita
                newGameButton.setEnabled(true);  // Abilita il bottone per una nuova partita
                menuButton.setEnabled(true);  // Abilita il bottone per tornare al menu
                logger.info("Il giocatore ha vinto. Totale vittorie: {}", playerWins);
            } else if (isBoardFull()) {  // Se la griglia è piena e non ci sono vincitori
                gameOver = true;
                draws++;
                JOptionPane.showMessageDialog(this, "Pareggio ... ci riproviamo?");
                DatabaseManager.incrementPareggi(username);  // Incrementa i pareggi nel database
                updateScore();  // Aggiorna il punteggio
                showMoveSummary();  // Mostra il riepilogo della partita
                newGameButton.setEnabled(true);  // Abilita il bottone per una nuova partita
                menuButton.setEnabled(true);  // Abilita il bottone per tornare al menu
                logger.info("Pareggio rilevato.");
            } else {
                currentPlayer = 'O';  // Passa il turno al bot
                handleBotMove();  // Gestisce la mossa del bot
            }
        }
    }

    // Gestisce la mossa del bot
    private void handleBotMove() {
        int botMove = "facile".equalsIgnoreCase(difficulty) ? calculateRandomMove() : calculateMinimaxMove();

        int row = botMove / 3;
        int col = botMove % 3;
        board[row][col] = 'O';
        buttons[botMove].setText("O");
        buttons[botMove].setEnabled(false);
        totalMoves++;
        moveHistory.add("Bot: O in posizione (" + row + "," + col + ")");
        logger.info("Mossa del bot O: ({}, {})", row, col);

        if (checkWin()) {  // Se il bot ha vinto
            gameOver = true;
            botWins++;
            highlightWinningCells('O');  // Evidenzia le celle vincenti
            JOptionPane.showMessageDialog(this, "Hai perso, ma non mollare! Riprova");
            DatabaseManager.incrementSconfitte(username);  // Incrementa le sconfitte nel database
            updateScore();  // Aggiorna il punteggio
            showMoveSummary();  // Mostra il riepilogo della partita
            newGameButton.setEnabled(true);  // Abilita il bottone per una nuova partita
            menuButton.setEnabled(true);  // Abilita il bottone per tornare al menu
            logger.info("Il bot ha vinto. Totale sconfitte: {}", botWins);
        } else if (isBoardFull()) {  // Se la griglia è piena e non ci sono vincitori
            gameOver = true;
            draws++;
            JOptionPane.showMessageDialog(this, "Pareggio ... ci riproviamo?");
            DatabaseManager.incrementPareggi(username);  // Incrementa i pareggi nel database
            updateScore();  // Aggiorna il punteggio
            showMoveSummary();  // Mostra il riepilogo della partita
            newGameButton.setEnabled(true);  // Abilita il bottone per una nuova partita
            menuButton.setEnabled(true);  // Abilita il bottone per tornare al menu
            logger.info("Pareggio dopo la mossa del bot.");
        } else {
            currentPlayer = 'X';  // Passa il turno al giocatore
        }
    }

    // Calcola la mossa del bot in modalità facile (mossa casuale)
    private int calculateRandomMove() {
        Random rand = new Random();
        int move;
        do {
            move = rand.nextInt(9);  // Genera una mossa casuale tra 0 e 8
        } while (board[move / 3][move % 3] != ' ');  // Continua finché la cella non è vuota
        return move;
    }

    // Calcola la mossa del bot in modalità difficile (usando l'algoritmo Minimax)
    private int calculateMinimaxMove() {
        try {
            logger.debug("Calcolo mossa con algoritmo Minimax...");
            int bestScore = Integer.MIN_VALUE;
            int bestMove = -1;

            for (int i = 0; i < 9; i++) {
                int row = i / 3;
                int col = i % 3;
                if (board[row][col] == ' ') {
                    board[row][col] = 'O';
                    int score = minimax(0, false);
                    board[row][col] = ' ';
                    if (score > bestScore) {
                        bestScore = score;
                        bestMove = i;
                    }
                }
            }
            return bestMove;
        } catch (Exception ex) {
            logger.error("Errore nel calcolare la mossa Minimax", ex);
            return calculateRandomMove();  // Fallisce e ritorna alla mossa casuale se c'è un errore
        }
    }

    // Algoritmo Minimax per calcolare la mossa migliore per il bot
    private int minimax(int depth, boolean isMaximizing) {
        if (checkWin()) return isMaximizing ? -1 : 1;  // Se il gioco è finito, restituisci il punteggio
        if (isBoardFull()) return 0;  // Pareggio

        if (isMaximizing) {
            int bestScore = Integer.MIN_VALUE;
            for (int i = 0; i < 9; i++) {
                int row = i / 3;
                int col = i % 3;
                if (board[row][col] == ' ') {
                    board[row][col] = 'O';
                    int score = minimax(depth + 1, false);
                    board[row][col] = ' ';
                    bestScore = Math.max(score, bestScore);
                }
            }
            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;
            for (int i = 0; i < 9; i++) {
                int row = i / 3;
                int col = i % 3;
                if (board[row][col] == ' ') {
                    board[row][col] = 'X';
                    int score = minimax(depth + 1, true);
                    board[row][col] = ' ';
                    bestScore = Math.min(score, bestScore);
                }
            }
            return bestScore;
        }
    }

    // Verifica se qualcuno ha vinto
    private boolean checkWin() {
        for (int i = 0; i < 3; i++) {
            // Controlla le righe e le colonne
            if ((board[i][0] == board[i][1] && board[i][1] == board[i][2] && board[i][0] != ' ') ||
                (board[0][i] == board[1][i] && board[1][i] == board[2][i] && board[0][i] != ' ')) {
                return true;
            }
        }
        // Controlla le diagonali
        if ((board[0][0] == board[1][1] && board[1][1] == board[2][2] && board[0][0] != ' ') ||
            (board[0][2] == board[1][1] && board[1][1] == board[2][0] && board[0][2] != ' ')) {
            return true;
        }
        return false;
    }

    // Verifica se la griglia è piena (pareggio)
    private boolean isBoardFull() {
        for (int i = 0; i < 9; i++) {
            if (board[i / 3][i % 3] == ' ') {
                return false;
            }
        }
        return true;
    }

    // Evidenzia le celle vincenti
    private void highlightWinningCells(char player) {
        // Controlla righe
        for (int i = 0; i < 3; i++) {
            if (board[i][0] == player && board[i][1] == player && board[i][2] == player) {
                for (int j = 0; j < 3; j++) {
                    buttons[i * 3 + j].setBackground(player == 'X' ? Color.GREEN : Color.RED);
                }
                return;
            }
        }

        // Controlla colonne
        for (int i = 0; i < 3; i++) {
            if (board[0][i] == player && board[1][i] == player && board[2][i] == player) {
                for (int j = 0; j < 3; j++) {
                    buttons[j * 3 + i].setBackground(player == 'X' ? Color.GREEN : Color.RED);
                }
                return;
            }
        }

        // Controlla diagonale principale
        if (board[0][0] == player && board[1][1] == player && board[2][2] == player) {
            for (int i = 0; i < 3; i++) {
                buttons[i * 3 + i].setBackground(player == 'X' ? Color.GREEN : Color.RED);
            }
            return;
        }

        // Controlla diagonale secondaria
        if (board[0][2] == player && board[1][1] == player && board[2][0] == player) {
            for (int i = 0; i < 3; i++) {
                buttons[i * 3 + (2 - i)].setBackground(player == 'X' ? Color.GREEN : Color.RED);
            }
        }
    }

    // Mostra il riepilogo della partita
    private void showMoveSummary() {
        StringBuilder summary = new StringBuilder();
        for (String move : moveHistory) {
            summary.append(move).append("\n");
        }
        JOptionPane.showMessageDialog(this, summary.toString(), "Riepilogo della partita", JOptionPane.INFORMATION_MESSAGE);
    }

    // Aggiorna il punteggio visualizzato
    private void updateScore() {
        scoreLabel.setText("Vittorie: " + playerWins + " - Sconfitte: " + botWins + " - Pareggi: " + draws);
    }

    // Avvia una nuova partita
    private void startNewGame() {
        resetBoard();  // Resetta la griglia
        newGameButton.setEnabled(false);  // Disabilita il bottone di nuova partita
        menuButton.setEnabled(false);  // Disabilita il bottone del menu
    }
}
