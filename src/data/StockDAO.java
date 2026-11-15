package data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.Stock;

/**
 * Saves and loads the listed stocks, so prices survive a restart.
 */
public class StockDAO {

    private final Connection connection;

    public StockDAO(DatabaseManager database) {
        this.connection = database.getConnection();
    }

    /**
     * Load every stock. The first time the program runs the table is
     * empty, so the four default stocks are inserted first.
     */
    public List<Stock> loadAll() throws SQLException {
        if (isEmpty()) {
            insert(new Stock("TECH", "TechNova Ltd", 100.00));
            insert(new Stock("GRNE", "GreenEnergy Corp", 50.00));
            insert(new Stock("MEDI", "MediCare Labs", 200.00));
            insert(new Stock("FOOD", "FoodBazaar Inc", 25.00));
        }

        List<Stock> stocks = new ArrayList<>();
        String sql = "SELECT symbol, name, last_price FROM stocks ORDER BY rowid";
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            while (result.next()) {
                stocks.add(new Stock(result.getString("symbol"), result.getString("name"),
                        result.getDouble("last_price")));
            }
        }
        return stocks;
    }

    private boolean isEmpty() throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM stocks")) {
            result.next();
            return result.getInt(1) == 0;
        }
    }

    private void insert(Stock stock) throws SQLException {
        String sql = "INSERT INTO stocks (symbol, name, last_price) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, stock.getSymbol());
            statement.setString(2, stock.getName());
            statement.setDouble(3, stock.getLastPrice());
            statement.executeUpdate();
        }
    }

    /** Store the stock's current price. Called after every trade. */
    public void updatePrice(Stock stock) throws SQLException {
        String sql = "UPDATE stocks SET last_price = ? WHERE symbol = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, stock.getLastPrice());
            statement.setString(2, stock.getSymbol());
            statement.executeUpdate();
        }
    }
}
