package data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.Trade;

/**
 * Saves completed trades and reads the trade history back.
 */
public class TradeDAO {

    private final Connection connection;

    public TradeDAO(DatabaseManager database) {
        this.connection = database.getConnection();
    }

    public void save(Trade trade) throws SQLException {
        String sql = "INSERT INTO trades (time, symbol, buyer, seller, quantity, price)"
                + " VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, trade.getFormattedTime());
            statement.setString(2, trade.getStock().getSymbol());
            statement.setString(3, trade.getBuyer().getName());
            statement.setString(4, trade.getSeller().getName());
            statement.setInt(5, trade.getQuantity());
            statement.setDouble(6, trade.getPrice());
            statement.executeUpdate();
        }
    }

    /** The newest trades, newest first. Used by the history table. */
    public List<TradeRecord> loadRecent(int limit) throws SQLException {
        String sql = "SELECT id, time, symbol, buyer, seller, quantity, price"
                + " FROM trades ORDER BY id DESC LIMIT ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            return readRecords(statement);
        }
    }

    /** Every trade ever made, oldest first. Used by the CSV export. */
    public List<TradeRecord> loadAll() throws SQLException {
        String sql = "SELECT id, time, symbol, buyer, seller, quantity, price"
                + " FROM trades ORDER BY id";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            return readRecords(statement);
        }
    }

    private List<TradeRecord> readRecords(PreparedStatement statement) throws SQLException {
        List<TradeRecord> records = new ArrayList<>();
        try (ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                records.add(new TradeRecord(
                        result.getInt("id"),
                        result.getString("time"),
                        result.getString("symbol"),
                        result.getString("buyer"),
                        result.getString("seller"),
                        result.getInt("quantity"),
                        result.getDouble("price")));
            }
        }
        return records;
    }
}
