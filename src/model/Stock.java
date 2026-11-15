package model;

/**
 * One company listed on our exchange.
 *
 * The "last price" is simply the price of the most recent trade.
 * Nobody sets it directly: it changes only when two orders match.
 */
public class Stock {

    private final String symbol;   // short code, e.g. "TECH"
    private final String name;     // full name, e.g. "TechNova Ltd"
    private double lastPrice;      // price of the most recent trade

    public Stock(String symbol, String name, double openingPrice) {
        this.symbol = symbol;
        this.name = name;
        this.lastPrice = openingPrice;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public double getLastPrice() {
        return lastPrice;
    }

    /** Called by the matching engine every time a trade happens. */
    public void setLastPrice(double lastPrice) {
        this.lastPrice = lastPrice;
    }

    @Override
    public String toString() {
        return symbol + " (" + name + ") @ " + String.format("%.2f", lastPrice);
    }
}
