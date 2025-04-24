package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import database.DatabaseManager;  // Per registrare l'utente
import gui.MainMenuFrame;         // Per tornare al menu principale
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RegisterFrame extends JFrame {

    private static final Logger logger = LogManager.getLogger(RegisterFrame.class); // Logger per monitorare le azioni dell'utente

    private JTextField usernameField; // Campo di testo per inserire il nome utente
    private JPasswordField passwordField; // Campo per inserire la password
    private JPasswordField confirmPasswordField; // Campo per confermare la password
    private JButton registerButton; // Pulsante per registrarsi
    private JButton backButton; // Pulsante per tornare al menu principale

    // Costruttore della finestra di registrazione
    public RegisterFrame() {
        setTitle("Registrazione"); // Imposta il titolo della finestra
        setSize(300, 300); // Imposta la dimensione della finestra
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Imposta la chiusura dell'applicazione alla chiusura della finestra
        setLocationRelativeTo(null); // Centra la finestra sullo schermo
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS)); // Layout verticale per i componenti

        // Etichetta e campo di testo per il nome utente
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        usernameField = new JTextField(20);
        usernameField.setMaximumSize(new Dimension(200, 25));
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Etichetta e campo per la password
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordField = new JPasswordField(20);
        passwordField.setMaximumSize(new Dimension(200, 25));
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Etichetta e campo per confermare la password
        JLabel confirmLabel = new JLabel("Conferma Password:");
        confirmLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setMaximumSize(new Dimension(200, 25));
        confirmPasswordField.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Pulsante di registrazione
        registerButton = new JButton("Registrati");
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.setMaximumSize(new Dimension(150, 30));

        // Pulsante per tornare al menu principale
        backButton = new JButton("Torna al menu");
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.setMaximumSize(new Dimension(150, 30));

        // Aggiunta dei componenti alla finestra con spaziatura verticale
        add(Box.createVerticalGlue());
        add(usernameLabel);
        add(usernameField);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(passwordLabel);
        add(passwordField);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(confirmLabel);
        add(confirmPasswordField);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(registerButton);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(backButton);
        add(Box.createVerticalGlue());

        // Listener per il pulsante di registrazione
        registerButton.addActionListener(e -> register());
        
        // Listener per il pulsante di ritorno al menu principale
        backButton.addActionListener(e -> goBackToMainMenu());
    }

    // Metodo per eseguire la registrazione dell'utente
    private void register() {
        // Ottiene i valori inseriti nei campi di testo
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String confirmPassword = new String(confirmPasswordField.getPassword()).trim();

        // Verifica che i campi non siano vuoti
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tutti i campi devono essere riempiti.");
            logger.warn("Tentativo di registrazione con campi vuoti.");
            return;
        }

        // Verifica che le password corrispondano
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Le password non corrispondono.");
            logger.warn("Le password inserite non corrispondono per l'utente: " + username);
            return;
        }

        try {
            // Chiamata al DatabaseManager per registrare l'utente
            boolean isRegistered = DatabaseManager.registerUser(username, password);
            
            // Se la registrazione è riuscita
            if (isRegistered) {
                JOptionPane.showMessageDialog(this, "Registrazione completata con successo!");
                logger.info("Registrazione riuscita per l'utente: " + username);
                goBackToMainMenu();
            } else {
                // Se l'username è già preso
                JOptionPane.showMessageDialog(this, "Username già esistente. Scegli un altro nome utente.");
                logger.warn("Tentativo di registrazione con username già esistente: " + username);
            }
        } catch (Exception ex) {
            // Gestione degli errori di registrazione
            if (ex.getMessage().toLowerCase().contains("duplicate entry")) {
                // Errore dovuto a un nome utente già esistente nel database
                JOptionPane.showMessageDialog(this, "Username già esistente. Scegli un altro nome utente.");
                logger.warn("Errore di duplicato durante la registrazione per l'utente: " + username);
            } else {
                // Altri errori durante la registrazione
                JOptionPane.showMessageDialog(this, "Errore durante la registrazione: " + ex.getMessage());
                logger.error("Errore durante la registrazione per l'utente " + username + ": " + ex.getMessage());
            }
        }
    }

    // Metodo per tornare al menu principale
    private void goBackToMainMenu() {
        new MainMenuFrame().setVisible(true); // Mostra la finestra del menu principale
        this.setVisible(false); // Nasconde la finestra corrente
        logger.info("L'utente è tornato al menu principale.");
    }
}
