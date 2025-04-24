package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import database.DatabaseManager;         // Gestione login utente
import gui.PostLoginMenuFrame;          // Finestra mostrata dopo il login
import gui.MainMenuFrame;               // Menu principale
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoginFrame extends JFrame {

    private static final Logger logger = LogManager.getLogger(LoginFrame.class);

    // Componenti della GUI: campi di testo e pulsanti
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton backButton;

    // Costruttore della finestra di login
    public LoginFrame() {
        setTitle("Login");
        setSize(300, 250); // Imposta la dimensione della finestra
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Comportamento di chiusura della finestra
        setLocationRelativeTo(null); // Centra la finestra
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS)); // Layout verticale per i componenti

        // Etichetta e campo di inserimento per l'username
        JLabel userLabel = new JLabel("Username:");
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Allinea al centro
        usernameField = new JTextField(20); // Imposta la lunghezza del campo di testo
        usernameField.setMaximumSize(new Dimension(200, 25)); // Imposta la dimensione massima del campo
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT); // Allinea al centro

        // Etichetta e campo di inserimento per la password
        JLabel passLabel = new JLabel("Password:");
        passLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordField = new JPasswordField(20); // Campo per la password
        passwordField.setMaximumSize(new Dimension(200, 25)); // Imposta la dimensione massima del campo
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT); // Allinea al centro

        // Pulsante di login
        loginButton = new JButton("Accedi");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT); // Allinea al centro
        loginButton.setMaximumSize(new Dimension(150, 30)); // Imposta la dimensione del pulsante

        // Pulsante per tornare al menu principale
        backButton = new JButton("Torna al menu");
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.setMaximumSize(new Dimension(150, 30));

        // Aggiungi tutti i componenti nella finestra con spaziatura verticale
        add(Box.createVerticalGlue()); // Aggiunge uno spazio flessibile prima dell'elemento
        add(userLabel);
        add(usernameField);
        add(Box.createRigidArea(new Dimension(0, 10))); // Spazio fisso tra i componenti
        add(passLabel);
        add(passwordField);
        add(Box.createRigidArea(new Dimension(0, 20))); // Spazio fisso tra i componenti
        add(loginButton);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(backButton);
        add(Box.createVerticalGlue()); // Aggiunge uno spazio flessibile dopo l'ultimo componente

        // Azioni associate ai pulsanti
        loginButton.addActionListener(e -> login()); // Quando si preme "Accedi", chiamata al metodo login()
        backButton.addActionListener(e -> goBackToMainMenu()); // Quando si preme "Torna al menu", ritorno al menu principale
    }

    // Metodo per eseguire il login
    private void login() {
        // Recupera il testo inserito nei campi
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim(); // Ottieni la password come stringa

        // Verifica se i campi sono vuoti
        if (username.isEmpty() || password.isEmpty()) {
            logger.warn("Tentativo di login con campi vuoti.");
            JOptionPane.showMessageDialog(this, "Tutti i campi devono essere riempiti.");
            return;
        }

        logger.info("Tentativo di login per l'utente: {}", username);
        
        // Verifica le credenziali tramite il DatabaseManager
        if (DatabaseManager.loginUser(username, password)) {
            logger.info("Login riuscito per l'utente: {}", username);
            // Se il login è riuscito, mostra il menu post-login e nascondi la finestra di login
            new PostLoginMenuFrame(username).setVisible(true); // Passa l'username al menu post-login
            this.setVisible(false);
        } else {
            logger.error("Credenziali errate per l'utente: {}", username);
            // Mostra un messaggio di errore se il login non riesce
            JOptionPane.showMessageDialog(this, "Credenziali errate. Riprova.");
        }
    }

    // Metodo per tornare al menu principale
    private void goBackToMainMenu() {
        logger.info("Ritorno al menu principale.");
        // Mostra il menu principale e nascondi la finestra di login
        new MainMenuFrame().setVisible(true);
        this.setVisible(false);
    }
}
