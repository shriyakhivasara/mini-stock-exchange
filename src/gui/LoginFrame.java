package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import model.HumanTrader;

/**
 * The first window: log in to an existing account or create a new one.
 */
public class LoginFrame extends JFrame {

    private final ExchangeServices services;

    private final JTextField usernameField = new JTextField(16);
    private final JPasswordField passwordField = new JPasswordField(16);

    public LoginFrame(ExchangeServices services) {
        super("Mini Stock Exchange - Login");
        this.services = services;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(buildTitle(), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        pack();
        setResizable(false);
        setLocationRelativeTo(null);   // centre on the screen
    }

    private JPanel buildTitle() {
        JLabel title = new JLabel("Mini Stock Exchange", SwingConstants.CENTER);
        title.setFont(Theme.TITLE_FONT);

        JLabel subtitle = new JLabel("Paper trading simulator", SwingConstants.CENTER);
        subtitle.setForeground(Theme.MUTED_TEXT);

        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 4));
        panel.setBorder(BorderFactory.createEmptyBorder(22, 40, 8, 40));
        panel.add(title);
        panel.add(subtitle);
        return panel;
    }

    private JPanel buildForm() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 40, 12, 40));
        panel.add(new JLabel("Username"));
        panel.add(usernameField);
        panel.add(new JLabel("Password"));
        panel.add(passwordField);
        return panel;
    }

    private JPanel buildButtons() {
        JButton loginButton = new JButton("Log in");
        JButton registerButton = new JButton("Create account");

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                logIn();
            }
        });
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                createAccount();
            }
        });

        // Pressing Enter anywhere in the window clicks "Log in".
        getRootPane().setDefaultButton(loginButton);

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(4, 40, 22, 40));
        panel.add(loginButton);
        panel.add(registerButton);
        return panel;
    }

    private void logIn() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both a username and a password.");
            return;
        }

        try {
            HumanTrader trader = services.login(username, password);
            if (trader == null) {
                showError("Wrong username or password.");
                return;
            }
            openMainWindow(trader);
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    private void createAccount() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError("Choose a username and a password first, then click Create account.");
            return;
        }
        if (password.length() < 4) {
            showError("The password must be at least 4 characters long.");
            return;
        }

        try {
            if (services.usernameExists(username)) {
                showError("The username \"" + username + "\" is already taken.");
                return;
            }

            String name = JOptionPane.showInputDialog(this, "Your name:", "Create account",
                    JOptionPane.QUESTION_MESSAGE);
            if (name == null) {
                return;                    // the user pressed Cancel
            }
            name = name.trim();
            if (name.isEmpty()) {
                showError("The name cannot be empty.");
                return;
            }

            HumanTrader trader = services.register(name, username, password);
            JOptionPane.showMessageDialog(this,
                    "Account created. You start with " + Theme.money(ExchangeServices.STARTING_CASH)
                            + " in cash.",
                    "Welcome", JOptionPane.INFORMATION_MESSAGE);
            openMainWindow(trader);
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    private void openMainWindow(HumanTrader trader) {
        MainFrame mainFrame = new MainFrame(services, trader);
        mainFrame.setVisible(true);
        dispose();                         // close the login window
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Mini Stock Exchange", JOptionPane.ERROR_MESSAGE);
    }
}
