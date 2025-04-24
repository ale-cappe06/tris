package database;

import model.UserStats;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/tris_game";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static final Logger logger = LogManager.getLogger(DatabaseManager.class);

    public static Connection getConnection() throws SQLException {
        try {
            logger.info("Tentativo di connessione al database...");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            logger.error("Errore di connessione al database: {}", e.getMessage());
            throw new SQLException("Errore di connessione al database: " + e.getMessage());
        }
    }

    private static String encryptPassword(String password) {
        try {
            logger.debug("Cifratura della password...");
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            logger.debug("Password cifrata.");
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("Errore nella cifratura della password: {}", e.getMessage());
            throw new RuntimeException("Errore nella cifratura della password: " + e.getMessage());
        }
    }

    public static boolean registerUser(String username, String password) {
        String query = "INSERT INTO users (username, password, vittorie, sconfitte, pareggi) VALUES (?, ?, 0, 0, 0)";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, encryptPassword(password));

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Nuovo utente registrato: {}", username);
                return true;
            }

            logger.warn("Registrazione dell'utente fallita: {}", username);
            return false;
        } catch (SQLException e) {
            logger.error("Errore durante la registrazione dell'utente: {}", e.getMessage());
            return false;
        }
    }

    public static boolean loginUser(String username, String password) {
        String query = "SELECT password FROM users WHERE username = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    boolean isValid = storedPassword.equals(encryptPassword(password));

                    if (isValid) {
                        logger.info("Login riuscito per l'utente: {}", username);
                    } else {
                        logger.warn("Password errata per l'utente: {}", username);
                    }

                    return isValid;
                }
            }
        } catch (SQLException e) {
            logger.error("Errore durante il login dell'utente: {}", e.getMessage());
        }
        return false;
    }

    // Metodo generico per incrementare le statistiche
    private static void updateStats(String username, String column) {
        String query = "UPDATE users SET " + column + " = " + column + " + 1 WHERE username = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
            logger.info("Statistiche aggiornate per l'utente: {} ({})", username, column);
        } catch (SQLException e) {
            logger.error("Errore nell'aggiornamento delle statistiche per l'utente {}: {}", username, e.getMessage());
        }
    }

    // Metodi specifici per aggiornare vittorie, sconfitte e pareggi
    public static void incrementVittorie(String username) {
        logger.info("Incremento delle vittorie per l'utente: {}", username);
        updateStats(username, "vittorie");
    }

    public static void incrementSconfitte(String username) {
        logger.info("Incremento delle sconfitte per l'utente: {}", username);
        updateStats(username, "sconfitte");
    }

    public static void incrementPareggi(String username) {
        logger.info("Incremento dei pareggi per l'utente: {}", username);
        updateStats(username, "pareggi");
    }

    public static UserStats getUserStats(String username) {
        String query = "SELECT vittorie, sconfitte, pareggi FROM users WHERE username = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    logger.info("Recupero delle statistiche per l'utente: {}", username);
                    return new UserStats(
                            rs.getInt("vittorie"),
                            rs.getInt("sconfitte"),
                            rs.getInt("pareggi"));
                }
            }
        } catch (SQLException e) {
            logger.error("Errore durante il recupero delle statistiche per l'utente {}: {}", username, e.getMessage());
        }
        return null;
    }
}
