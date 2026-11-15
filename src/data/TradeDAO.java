package data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.HumanTrader;
import model.Trade;
import model.Trader;

/**
 * Saves completed trades and reads the trade history back.
 */
public class TradeDAO {

    private static final String COLUMNS =
            "id, time, symbol, buyer, seller, buyer_username, seller_username, quantity, price";

    private final Connection connection;

    public TradeDAO(DatabaseManager database) {
        this.connection = database.getConnection();
    }

    public void save(Trade trade) throws SQLException {
        String sql = "INSERT INTO trades"
                + " (time, symbol, buyer, seller, buyer_username, seller_username, quantity, price)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, trade.getFormattedTime());
            statement.setString(2, trade.getStock().getSymbol());
            statement.setString(3, trade.getBuyer().getName());
            statement.setString(4, trade.getSeller().getName());
            statement.setString(5, usernameOf(trade.getBuyer()));
            statement.setString(6, usernameOf(trade.getSeller()));
            statement.setInt(7, trade.getQuantity());
            statement.setDouble(8, trade.getPrice());
            statement.executeUpdate();
        }
    }

    /** Only human traders have a username; bots are stored with an empty one. */
    private String usernameOf(Trader trader) {
        if (trader instanceof HumanTrader) {
            return ((HumanTrader) trader).getUsername();
        }
        return "";
    }

    /** The newest trades on the whole exchange, newest first. */
    public List<TradeRecord> loadRecent(int limit) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM trades ORDER BY id DESC LIMIT ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            return readRecords(statement);
        }
    }

    /** Every trade ever made, oldest first. Used by the CSV export. */
    public List<TradeRecord> loadAll() throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM trades ORDER BY id";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            return readRecords(statement);
        }
    }

    /** Every trade where this user was the buyer or the seller, oldest first. */
    public List<TradeRecord> loadForUser(String username) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM trades"
                + " WHERE buyer_username = ? OR seller_username = ? ORDER BY id";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, username);
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
                        result.getString("buyer_username"),
                        result.getString("seller_username"),
                        result.getInt("quantity"),
                        result.getDouble("price")));
            }
        }
        return records;
    }
}
