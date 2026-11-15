package engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Stock;
import model.Trade;

/**
 * Remembers the recent prices of every stock, for the price chart.
 *
 * It implements TradeListener: each time the engine reports a trade,
 * the trade's price is added to that stock's list.
 *
 * The methods are synchronized because trades arrive on the bot's
 * thread while the chart reads the prices on the GUI thread.
 */
public class PriceHistory implements TradeListener {

    private static final int MAX_POINTS = 300;   // keep the chart readable

    private final Map<String, List<Double>> pricesBySymbol = new HashMap<>();

    /** Start tracking a stock, beginning with its current price. */
    public synchronized void track(Stock stock) {
        List<Double> prices = new ArrayList<>();
        prices.add(stock.getLastPrice());
        pricesBySymbol.put(stock.getSymbol(), prices);
    }

    @Override
    public synchronized void onTrade(Trade trade) {
        List<Double> prices = pricesBySymbol.get(trade.getStock().getSymbol());
        if (prices == null) {
            return;
        }
        prices.add(trade.getPrice());
        if (prices.size() > MAX_POINTS) {
            prices.remove(0);   // drop the oldest point
        }
    }

    /** A copy of the prices for one stock, oldest first. */
    public synchronized List<Double> getPrices(String symbol) {
        List<Double> prices = pricesBySymbol.get(symbol);
        if (prices == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(prices);
    }
}
