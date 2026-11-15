package engine;

import model.Trade;

/**
 * Anything that wants to be told when a trade happens.
 *
 * The matching engine knows nothing about the GUI or the database.
 * It only knows it has a list of TradeListeners and calls onTrade()
 * on each one. The GUI and the database code implement this interface,
 * so they can react without the engine depending on them.
 */
public interface TradeListener {

    void onTrade(Trade trade);
}
