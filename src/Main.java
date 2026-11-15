import java.sql.SQLException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import bot.BotTrader;
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

    // Each bot waits between 0.7 and 1.4 seconds between actions.
    private static final int BOT_DELAY_MILLIS = 700;

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

        // 4. Two bot traders, each running on its own thread
        final BotTrader alpha = new BotTrader("Bot Alpha", engine, BOT_DELAY_MILLIS);
        final BotTrader beta = new BotTrader("Bot Beta", engine, BOT_DELAY_MILLIS);
        startOnNewThread(alpha);
        startOnNewThread(beta);

        // 5. Stop the bots and close the database when the program exits
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                alpha.stop();
                beta.stop();
                database.close();
            }
        }));

        // 6. Show the login window
        ExchangeServices services = new ExchangeServices(engine, priceHistory, userDAO, tradeDAO);
        new LoginFrame(services).setVisible(true);
    }

    private static void startOnNewThread(BotTrader bot) {
        Thread thread = new Thread(bot, bot.getName());
        // A daemon thread does not keep the program alive after the windows close.
        thread.setDaemon(true);
        thread.start();
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
