package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

import data.CsvExporter;
import data.DatabaseManager;
import data.DatabaseTradeListener;
import data.ProfitCalculator;
import data.StockDAO;
import data.StockProfit;
import data.TradeDAO;
import data.TradeRecord;
import data.UserDAO;
import engine.MatchingEngine;
import model.HumanTrader;
import model.LimitOrder;
import model.OrderSide;
import model.Stock;

/**
 * Checks the database and CSV code using a temporary database file,
 * so the real data/exchange.db is never touched.
 *
 * Run with:  java -cp "out:lib/*" test.DataTest     (use ; instead of : on Windows)
 */
public class DataTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        File databaseFile = File.createTempFile("exchange-test", ".db");
        File csvFile = File.createTempFile("trades-test", ".csv");
        databaseFile.delete();   // let SQLite create it fresh

        DatabaseManager database = new DatabaseManager(databaseFile.getPath());
        UserDAO userDAO = new UserDAO(database);
        StockDAO stockDAO = new StockDAO(database);
        TradeDAO tradeDAO = new TradeDAO(database);

        // --- Stocks ---
        List<Stock> stocks = stockDAO.loadAll();
        check("stocks: ten default stocks are created on first run", stocks.size() == 10);
        check("stocks: loading again does not create duplicates", stockDAO.loadAll().size() == 10);

        // --- Users ---
        check("users: a new username does not exist yet", !userDAO.usernameExists("asha"));
        userDAO.register("Asha", "asha", "pass123", 10000.00);
        HumanTrader ravi = userDAO.register("Ravi", "ravi", "pass456", 10000.00);
        check("users: username exists after registering", userDAO.usernameExists("asha"));
        check("users: wrong password is refused", userDAO.login("asha", "wrong") == null);
        check("users: unknown username is refused", userDAO.login("nobody", "pass123") == null);

        HumanTrader asha = userDAO.login("asha", "pass123");
        check("users: correct password logs in with the saved cash",
                asha != null && same(asha.getCash(), 10000.00) && asha.getName().equals("Asha"));

        // --- A real trade, saved through the listener ---
        Stock apple = stocks.get(0);
        check("stocks: the first stock is Apple", apple.getSymbol().equals("AAPL"));
        MatchingEngine engine = new MatchingEngine();
        engine.addStock(apple);
        engine.addTradeListener(new DatabaseTradeListener(userDAO, stockDAO, tradeDAO));

        ravi.addShares("AAPL", 50);
        engine.placeOrder(new LimitOrder(ravi, apple, OrderSide.SELL, 10, 101.00));
        engine.placeOrder(new LimitOrder(asha, apple, OrderSide.BUY, 10, 101.00));

        HumanTrader ashaReloaded = userDAO.login("asha", "pass123");
        HumanTrader raviReloaded = userDAO.login("ravi", "pass456");
        check("save: buyer's cash is saved after the trade", same(ashaReloaded.getCash(), 8990.00));
        check("save: buyer's new shares are saved", ashaReloaded.getSharesOf("AAPL") == 10);
        check("save: seller's cash and shares are saved",
                same(raviReloaded.getCash(), 11010.00) && raviReloaded.getSharesOf("AAPL") == 40);
        check("save: stock price is saved", same(stockDAO.loadAll().get(0).getLastPrice(), 101.00));

        List<TradeRecord> records = tradeDAO.loadAll();
        check("trades: one trade is stored", records.size() == 1);
        check("trades: stored trade has the right details",
                records.size() == 1
                        && records.get(0).getSymbol().equals("AAPL")
                        && records.get(0).getBuyer().equals("Asha")
                        && records.get(0).getSeller().equals("Ravi")
                        && records.get(0).getQuantity() == 10
                        && same(records.get(0).getPrice(), 101.00));
        check("trades: loadRecent respects its limit", tradeDAO.loadRecent(5).size() == 1);

        // --- One user's own history and profit ---
        // Asha now sells 4 of her 10 shares back to Ravi at 111, a gain of 10 per share.
        engine.placeOrder(new LimitOrder(ravi, apple, OrderSide.BUY, 4, 111.00));
        engine.placeOrder(new LimitOrder(asha, apple, OrderSide.SELL, 4, 111.00));

        List<TradeRecord> ashaTrades = tradeDAO.loadForUser("asha");
        check("my trades: both of Asha's trades are found", ashaTrades.size() == 2);
        check("my trades: the first was a purchase, the second a sale",
                ashaTrades.size() == 2
                        && ashaTrades.get(0).wasBoughtBy("asha") && !ashaTrades.get(0).wasSoldBy("asha")
                        && ashaTrades.get(1).wasSoldBy("asha") && !ashaTrades.get(1).wasBoughtBy("asha"));
        check("my trades: a user with no trades gets an empty list", tradeDAO.loadForUser("nobody").isEmpty());

        List<StockProfit> profits = new ProfitCalculator().calculate(ashaTrades, "asha");
        check("profit: one company in the summary", profits.size() == 1);
        StockProfit appleProfit = profits.get(0);
        check("profit: bought 10 at an average of 101",
                appleProfit.getSharesBought() == 10 && same(appleProfit.getAverageBuyPrice(), 101.00));
        check("profit: sold 4 at an average of 111",
                appleProfit.getSharesSold() == 4 && same(appleProfit.getAverageSellPrice(), 111.00));
        check("profit: 4 shares x 10 gain = 40 profit", same(appleProfit.getProfit(), 40.00));
        check("profit: margin is 40 / 404 = 9.90%", same(appleProfit.getProfitMarginPercent(), 40.0 / 404.0 * 100.0));
        check("profit: 6 shares are still held", appleProfit.getSharesHeld() == 6);

        // Average cost with purchases at two prices, then a sale at a loss.
        StockProfit mixed = new StockProfit("TEST");
        mixed.addPurchase(10, 100.00);
        mixed.addPurchase(10, 120.00);      // average cost is now 110
        mixed.addSale(5, 100.00);           // 5 shares sold 10 below cost
        check("profit: average buy price of 10 @ 100 and 10 @ 120 is 110", same(mixed.getAverageBuyPrice(), 110.00));
        check("profit: selling 5 at 100 is a loss of 50", same(mixed.getProfit(), -50.00));
        check("profit: the loss margin is -9.09%", same(mixed.getProfitMarginPercent(), -50.0 / 550.0 * 100.0));

        records = tradeDAO.loadAll().subList(0, 1);   // the CSV checks below use the first trade only

        // --- CSV ---
        int written = new CsvExporter().export(records, csvFile);
        String header;
        String firstRow;
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            header = reader.readLine();
            firstRow = reader.readLine();
        }
        check("csv: reports one trade written", written == 1);
        check("csv: first line is the header",
                "Trade ID,Time,Stock,Buyer,Seller,Quantity,Price,Value".equals(header));
        check("csv: second line holds the trade",
                firstRow != null && firstRow.endsWith("AAPL,Asha,Ravi,10,101.00,1010.00"));

        database.close();

        // --- A database file from an older version is rebuilt ---
        File oldFile = File.createTempFile("exchange-old", ".db");
        oldFile.delete();
        try (java.sql.Connection old = java.sql.DriverManager.getConnection("jdbc:sqlite:" + oldFile.getPath());
             java.sql.Statement statement = old.createStatement()) {
            statement.executeUpdate("CREATE TABLE stocks (symbol TEXT PRIMARY KEY, name TEXT, last_price REAL)");
            statement.executeUpdate("INSERT INTO stocks VALUES ('TECH', 'TechNova Ltd', 100)");
        }
        DatabaseManager upgraded = new DatabaseManager(oldFile.getPath());
        List<Stock> upgradedStocks = new StockDAO(upgraded).loadAll();
        check("upgrade: an old database is rebuilt with the ten current stocks",
                upgradedStocks.size() == 10 && upgradedStocks.get(0).getSymbol().equals("AAPL"));
        upgraded.close();
        oldFile.delete();

        databaseFile.delete();
        csvFile.delete();

        System.out.println();
        System.out.println(passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void check(String description, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("PASS  " + description);
        } else {
            failed++;
            System.out.println("FAIL  " + description);
        }
    }

    private static boolean same(double a, double b) {
        return Math.abs(a - b) < 0.0001;
    }
}
