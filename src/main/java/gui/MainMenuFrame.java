package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainMenuFrame extends JFrame {

    private static final Logger logger = LogManager.getLogger(MainMenuFrame.class); // Logger per tracciare gli eventi

    // Componenti della GUI: i pulsanti per il login e la registrazione
    private JButton loginButton;
    private JButton registerButton;

    // Costruttore della finestra del menu principale
    public MainMenuFrame() {
        setTitle("Benvenuto in Tris"); // Imposta il titolo della finestra
        setSize(300, 200); // Imposta la dimensione della finestra
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Comportamento di chiusura della finestra
        setLocationRelativeTo(null); // Centra la finestra sullo schermo
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS)); // Layout verticale per i componenti

        // Inizializza i pulsanti
        loginButton = new JButton("Login");
        registerButton = new JButton("Registrati");

        // Allinea i pulsanti al centro della finestra
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Imposta la dimensione massima dei pulsanti
        loginButton.setMaximumSize(new Dimension(150, 30));
        registerButton.setMaximumSize(new Dimension(150, 30));

        // Aggiungi i componenti alla finestra con spaziatura verticale
        add(Box.createVerticalGlue()); // Spazio flessibile sopra il primo pulsante
        add(loginButton); // Aggiungi il pulsante "Login"
        add(Box.createRigidArea(new Dimension(0, 15))); // Spazio fisso tra i pulsanti
        add(registerButton); // Aggiungi il pulsante "Registrati"
        add(Box.createVerticalGlue()); // Spazio flessibile sotto l'ultimo pulsante

        // Azioni associate ai pulsanti
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logger.info("L'utente ha scelto di fare il login."); // Log dell'evento
                showLoginScreen(); // Mostra la finestra di login
            }
        });

        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logger.info("L'utente ha scelto di registrarsi."); // Log dell'evento
                showRegisterScreen(); // Mostra la finestra di registrazione
            }
        });
    }

    // Metodo per mostrare la finestra di login
    private void showLoginScreen() {
        new LoginFrame().setVisible(true); // Crea e mostra la finestra di login
        this.setVisible(false); // Nascondi la finestra del menu principale
    }

    // Metodo per mostrare la finestra di registrazione
    private void showRegisterScreen() {
        new RegisterFrame().setVisible(true); // Crea e mostra la finestra di registrazione
        this.setVisible(false); // Nascondi la finestra del menu principale
    }

    // Metodo main per avviare l'applicazione
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MainMenuFrame().setVisible(true); // Mostra la finestra del menu principale
            }
        });
    }
}
