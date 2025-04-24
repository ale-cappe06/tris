package model;

import java.io.Serializable;  // Importa la classe Serializable per consentire la serializzazione degli oggetti

public class Move implements Serializable {

    // Righe e colonne della griglia in cui è stata effettuata la mossa
    public int row;  
    public int col;

    // Il simbolo del giocatore che ha effettuato la mossa (X o O)
    public PlayerSymbol symbol;

    // Costruttore che inizializza la mossa con la riga, la colonna e il simbolo
    public Move(int row, int col, PlayerSymbol symbol) {
        this.row = row;  // Imposta la riga della mossa
        this.col = col;  // Imposta la colonna della mossa
        this.symbol = symbol;  // Imposta il simbolo del giocatore (X o O)
    }
}