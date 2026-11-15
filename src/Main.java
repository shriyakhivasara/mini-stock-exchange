import java.sql.SQLException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import data.DatabaseManager;
import data.DatabaseTradeListener;
import data.StockDAO;
import data.TradeDAO;
import data.UserDAO;
import engine.MatchingEngine;
import engine.PriceHistory;
import gui.ExchangeServices;
import gui.LoginFrame;
import model.Stock;

/**
 * Program entry point. It builds every part of the application,
 * connects the parts to each other, and opens the login window.
 */
public class Main {

    public static void main(String[] args) {
        // All Swing work must happen on the GUI thread.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                start();
            }
        });
    }

    private static void start() {
        useNimbusLookAndFeel();

        // 1. Database
        final DatabaseManager database;
        UserDAO userDAO;
        StockDAO stockDAO;
        TradeDAO tradeDAO;
        MatchingEngine engine = new MatchingEngine();
        PriceHistory priceHistory = new PriceHistory();

        try {
            database = new DatabaseManager();
            userDAO = new UserDAO(database);
            stockDAO = new StockDAO(database);
            tradeDAO = new TradeDAO(database);

            // 2. Engine, with the stocks loaded from the database
            for (Stock stock : stockDAO.loadAll()) {
                engine.addStock(stock);
                priceHistory.track(stock);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Could not open the database:\n" + e.getMessage(),
                    "Mini Stock Exchange", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 3. Listeners: who gets told about each trade
        engine.addTradeListener(new DatabaseTradeListener(userDAO, stockDAO, tradeDAO));
        engine.addTradeListener(priceHistory);

        // 4. Close the database cleanly when the program exits
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                database.close();
            }
        }));

        // 5. Show the login window
        ExchangeServices services = new ExchangeServices(engine, priceHistory, userDAO, tradeDAO);
        new LoginFrame(services).setVisible(true);
    }

    /** Nimbus is a modern-looking theme that ships with Java. */
    private static void useNimbusLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                }
            }
        } catch (Exception e) {
            // If Nimbus is missing, Swing keeps its default theme.
            System.err.println("Could not set the Nimbus theme: " + e.getMessage());
        }
    }
}
