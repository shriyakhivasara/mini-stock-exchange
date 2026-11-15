package data;

import java.sql.SQLException;

import engine.TradeListener;
import model.HumanTrader;
import model.Trade;
import model.Trader;

/**
 * Saves every trade to the database as soon as it happens.
 *
 * It implements TradeListener, so the matching engine calls onTrade()
 * without knowing a database exists. For each trade it stores:
 *   - the trade itself,
 *   - the stock's new price,
 *   - the new cash and holdings of any human trader involved.
 * Bot traders are not saved; they start fresh every time.
 */
public class DatabaseTradeListener implements TradeListener {

    private final UserDAO userDAO;
    private final StockDAO stockDAO;
    private final TradeDAO tradeDAO;

    public DatabaseTradeListener(UserDAO userDAO, StockDAO stockDAO, TradeDAO tradeDAO) {
        this.userDAO = userDAO;
        this.stockDAO = stockDAO;
        this.tradeDAO = tradeDAO;
    }

    @Override
    public void onTrade(Trade trade) {
        try {
            tradeDAO.save(trade);
            stockDAO.updatePrice(trade.getStock());
            saveIfHuman(trade.getBuyer());
            saveIfHuman(trade.getSeller());
        } catch (SQLException e) {
            // A listener cannot throw a checked exception back into the
            // engine, so report the problem and let trading continue.
            System.err.println("Could not save trade to the database: " + e.getMessage());
        }
    }

    private void saveIfHuman(Trader trader) throws SQLException {
        if (trader instanceof HumanTrader) {
            userDAO.save((HumanTrader) trader);
        }
    }
}
