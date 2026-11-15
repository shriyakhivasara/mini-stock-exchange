package data;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Opens the SQLite database file and makes sure its tables exist.
 *
 * SQLite stores the whole database in one file (data/exchange.db), so
 * there is no database server to install. We talk to it through JDBC,
 * Java's standard database interface, using the driver jar in lib/.
 *
 * The other classes in this package (the DAOs) borrow the connection
 * from here to run their SQL.
 */
public class DatabaseManager {

    public static final String DEFAULT_FILE = "data/exchange.db";

    private final Connection connection;

    /** Open the normal database file used by the application. */
    public DatabaseManager() throws SQLException {
        this(DEFAULT_FILE);
    }

    /** Open a database at any path (the tests use a temporary file). */
    public DatabaseManager(String filePath) throws SQLException {
        // Make sure the folder exists, or SQLite cannot create the file.
        File folder = new File(filePath).getAbsoluteFile().getParentFile();
        if (folder != null) {
            folder.mkdirs();
        }
        connection = DriverManager.getConnection("jdbc:sqlite:" + filePath);
        createTables();
    }

    /** "IF NOT EXISTS" makes this safe to run every time the program starts. */
    private void createTables() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS users ("
                    + " username TEXT PRIMARY KEY,"
                    + " name     TEXT NOT NULL,"
                    + " password TEXT NOT NULL,"
                    + " cash     REAL NOT NULL)");

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS holdings ("
                    + " username TEXT NOT NULL,"
                    + " symbol   TEXT NOT NULL,"
                    + " shares   INTEGER NOT NULL,"
                    + " PRIMARY KEY (username, symbol))");

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS stocks ("
                    + " symbol     TEXT PRIMARY KEY,"
                    + " name       TEXT NOT NULL,"
                    + " last_price REAL NOT NULL)");

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS trades ("
                    + " id       INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + " time     TEXT NOT NULL,"
                    + " symbol   TEXT NOT NULL,"
                    + " buyer    TEXT NOT NULL,"
                    + " seller   TEXT NOT NULL,"
                    + " quantity INTEGER NOT NULL,"
                    + " price    REAL NOT NULL)");
        }
    }

    public Connection getConnection() {
        return connection;
    }

    /** Call once when the program exits. */
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            System.err.println("Could not close the database: " + e.getMessage());
        }
    }
}
