package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

import data.CsvExporter;
import data.DatabaseManager;
import data.DatabaseTradeListener;
import data.StockDAO;
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
        check("stocks: four default stocks are created on first run", stocks.size() == 4);
        check("stocks: loading again does not create duplicates", stockDAO.loadAll().size() == 4);

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
        Stock tech = stocks.get(0);
        MatchingEngine engine = new MatchingEngine();
        engine.addStock(tech);
        engine.addTradeListener(new DatabaseTradeListener(userDAO, stockDAO, tradeDAO));

        ravi.addShares("TECH", 50);
        engine.placeOrder(new LimitOrder(ravi, tech, OrderSide.SELL, 10, 101.00));
        engine.placeOrder(new LimitOrder(asha, tech, OrderSide.BUY, 10, 101.00));

        HumanTrader ashaReloaded = userDAO.login("asha", "pass123");
        HumanTrader raviReloaded = userDAO.login("ravi", "pass456");
        check("save: buyer's cash is saved after the trade", same(ashaReloaded.getCash(), 8990.00));
        check("save: buyer's new shares are saved", ashaReloaded.getSharesOf("TECH") == 10);
        check("save: seller's cash and shares are saved",
                same(raviReloaded.getCash(), 11010.00) && raviReloaded.getSharesOf("TECH") == 40);
        check("save: stock price is saved", same(stockDAO.loadAll().get(0).getLastPrice(), 101.00));

        List<TradeRecord> records = tradeDAO.loadAll();
        check("trades: one trade is stored", records.size() == 1);
        check("trades: stored trade has the right details",
                records.size() == 1
                        && records.get(0).getSymbol().equals("TECH")
                        && records.get(0).getBuyer().equals("Asha")
                        && records.get(0).getSeller().equals("Ravi")
                        && records.get(0).getQuantity() == 10
                        && same(records.get(0).getPrice(), 101.00));
        check("trades: loadRecent respects its limit", tradeDAO.loadRecent(5).size() == 1);

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
                firstRow != null && firstRow.endsWith("TECH,Asha,Ravi,10,101.00,1010.00"));

        database.close();
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
