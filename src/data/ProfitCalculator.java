package data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Turns one user's list of trades into a profit summary per stock.
 */
public class ProfitCalculator {

    /**
     * @param records  the user's trades, OLDEST FIRST (the order matters,
     *                 because a sale's profit depends on earlier purchases)
     * @param username whose point of view to take
     * @return one StockProfit per stock the user has traded, in the order
     *         the user first traded them
     */
    public List<StockProfit> calculate(List<TradeRecord> records, String username) {
        // LinkedHashMap remembers the order in which stocks were first seen.
        Map<String, StockProfit> bySymbol = new LinkedHashMap<>();

        for (TradeRecord record : records) {
            StockProfit summary = bySymbol.get(record.getSymbol());
            if (summary == null) {
                summary = new StockProfit(record.getSymbol());
                bySymbol.put(record.getSymbol(), summary);
            }

            if (record.wasBoughtBy(username)) {
                summary.addPurchase(record.getQuantity(), record.getPrice());
            }
            if (record.wasSoldBy(username)) {
                summary.addSale(record.getQuantity(), record.getPrice());
            }
        }
        return new ArrayList<>(bySymbol.values());
    }
}
