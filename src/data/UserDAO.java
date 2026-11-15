package data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import model.HumanTrader;

/**
 * Saves and loads user accounts. (DAO = Data Access Object: a class
 * whose only job is to move one kind of object in and out of the database.)
 *
 * Every query uses a PreparedStatement with "?" placeholders. The values
 * are filled in separately, so text typed by a user can never be run as SQL.
 */
public class UserDAO {

    private final Connection connection;

    public UserDAO(DatabaseManager database) {
        this.connection = database.getConnection();
    }

    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    /** Create a new account and return it as a trader ready to use. */
    public HumanTrader register(String name, String username, String password, double startingCash)
            throws SQLException {
        String sql = "INSERT INTO users (username, name, password, cash) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, name);
            statement.setString(3, password);
            statement.setDouble(4, startingCash);
            statement.executeUpdate();
        }
        return new HumanTrader(name, username, password, startingCash);
    }

    /**
     * Check a username and password.
     *
     * @return the trader with their cash and holdings loaded,
     *         or null if the username or password is wrong
     */
    public HumanTrader login(String username, String password) throws SQLException {
        HumanTrader trader = null;

        String sql = "SELECT name, password, cash FROM users WHERE username = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next() && result.getString("password").equals(password)) {
                    trader = new HumanTrader(result.getString("name"), username, password,
                            result.getDouble("cash"));
                }
            }
        }
        if (trader == null) {
            return null;
        }

        String holdingsSql = "SELECT symbol, shares FROM holdings WHERE username = ?";
        try (PreparedStatement statement = connection.prepareStatement(holdingsSql)) {
            statement.setString(1, username);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    trader.addShares(result.getString("symbol"), result.getInt("shares"));
                }
            }
        }
        return trader;
    }

    /** Write the trader's current cash and holdings back to the database. */
    public void save(HumanTrader trader) throws SQLException {
        String cashSql = "UPDATE users SET cash = ? WHERE username = ?";
        try (PreparedStatement statement = connection.prepareStatement(cashSql)) {
            statement.setDouble(1, trader.getCash());
            statement.setString(2, trader.getUsername());
            statement.executeUpdate();
        }

        // Simplest way to keep holdings correct: delete the old rows, insert the new ones.
        String deleteSql = "DELETE FROM holdings WHERE username = ?";
        try (PreparedStatement statement = connection.prepareStatement(deleteSql)) {
            statement.setString(1, trader.getUsername());
            statement.executeUpdate();
        }

        String insertSql = "INSERT INTO holdings (username, symbol, shares) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
            for (Map.Entry<String, Integer> holding : trader.getHoldings().entrySet()) {
                statement.setString(1, trader.getUsername());
                statement.setString(2, holding.getKey());
                statement.setInt(3, holding.getValue());
                statement.executeUpdate();
            }
        }
    }
}
