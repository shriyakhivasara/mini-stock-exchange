package gui;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import data.TradeDAO;
import data.UserDAO;
import engine.MatchingEngine;
import engine.PriceHistory;
import model.HumanTrader;

/**
 * The shared objects every screen needs, bundled together so they can
 * be passed around as one parameter instead of four.
 */
public class ExchangeServices {

    public static final double STARTING_CASH = 100000.00;

    private final MatchingEngine engine;
    private final PriceHistory priceHistory;
    private final UserDAO userDAO;
    private final TradeDAO tradeDAO;

    // Traders who have logged in since the program started.
    private final Map<String, HumanTrader> activeTraders = new HashMap<>();

    public ExchangeServices(MatchingEngine engine, PriceHistory priceHistory,
                            UserDAO userDAO, TradeDAO tradeDAO) {
        this.engine = engine;
        this.priceHistory = priceHistory;
        this.userDAO = userDAO;
        this.tradeDAO = tradeDAO;
    }

    /**
     * Check a username and password.
     *
     * If the same user logs out and back in, they get the SAME trader
     * object as before. Their waiting orders in the book still point to
     * that object, so creating a second copy would split their account.
     *
     * @return the trader, or null if the username or password is wrong
     */
    public HumanTrader login(String username, String password) throws SQLException {
        HumanTrader loaded = userDAO.login(username, password);
        if (loaded == null) {
            return null;
        }
        if (activeTraders.containsKey(username)) {
            return activeTraders.get(username);
        }
        activeTraders.put(username, loaded);
        return loaded;
    }

    /** Create a new account with the standard starting cash. */
    public HumanTrader register(String name, String username, String password) throws SQLException {
        HumanTrader trader = userDAO.register(name, username, password, STARTING_CASH);
        activeTraders.put(username, trader);
        return trader;
    }

    public boolean usernameExists(String username) throws SQLException {
        return userDAO.usernameExists(username);
    }

    public MatchingEngine getEngine() {
        return engine;
    }

    public PriceHistory getPriceHistory() {
        return priceHistory;
    }

    public TradeDAO getTradeDAO() {
        return tradeDAO;
    }
}
