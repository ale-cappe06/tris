package network;

import java.io.Serializable;  // Importa Serializable per consentire la serializzazione dell'oggetto
import model.PlayerSymbol;  // Importa PlayerSymbol per gestire i simboli dei giocatori (X e O)
import org.apache.logging.log4j.LogManager;  // Importa la libreria di logging log4j
import org.apache.logging.log4j.Logger;  // Importa la classe Logger per il logging

// Classe che rappresenta lo stato di una partita di Tic-Tac-Toe
public class GameState implements Serializable {
    private static final Logger logger = LogManager.getLogger(GameState.class);  // Logger per tracciare gli eventi

    public char[][] board = new char[3][3];  // La griglia del gioco 3x3, inizializzata con caratteri vuoti
    public PlayerSymbol currentPlayer;  // Il simbolo del giocatore attuale (X o O)
    public boolean gameOver = false;  // Flag che indica se la partita è terminata
    public String message = "";  // Messaggio da inviare ai giocatori (es. "È il turno di X")

    // Costruttore che inizializza lo stato del gioco
    public GameState() {
        // Inizializza la griglia con caratteri vuoti ('-')
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                board[i][j] = '-';
        
        currentPlayer = PlayerSymbol.X;  // Il primo giocatore (X) inizia
        message = "È il turno di " + currentPlayer;  // Inizializza il messaggio

        // Log del primo stato del gioco
        logger.info("GameState inizializzato: board vuota, currentPlayer = {}", currentPlayer);
    }

    // Metodo che aggiorna il messaggio di stato in base allo stato del gioco
    public void updateMessage() {
        if (gameOver) {  // Se il gioco è finito
            message = "Game Over!";  // Imposta il messaggio di game over
            logger.info("Game over. Messaggio aggiornato: {}", message);
        } else {  // Altrimenti, è il turno del giocatore attuale
            message = "È il turno di " + currentPlayer;  // Aggiorna il messaggio con il giocatore attuale
            logger.info("Messaggio aggiornato: {}", message);
        }
    }
}
