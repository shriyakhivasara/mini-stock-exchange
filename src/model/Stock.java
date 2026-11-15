package model;

/**
 * One company listed on our exchange.
 *
 * The "last price" is simply the price of the most recent trade.
 * Nobody sets it directly: it changes only when two orders match.
 *
 * The price methods are synchronized because the bot thread changes
 * the price while the GUI thread reads it.
 */
public class Stock {

    private final String symbol;     // short code, e.g. "TECH"
    private final String name;       // full name, e.g. "TechNova Ltd"
    private final double openPrice;  // price when the program started
    private double lastPrice;        // price of the most recent trade

    public Stock(String symbol, String name, double openingPrice) {
        this.symbol = symbol;
        this.name = name;
        this.openPrice = openingPrice;
        this.lastPrice = openingPrice;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public double getOpenPrice() {
        return openPrice;
    }

    public synchronized double getLastPrice() {
        return lastPrice;
    }

    /** Called by the matching engine every time a trade happens. */
    public synchronized void setLastPrice(double lastPrice) {
        this.lastPrice = lastPrice;
    }

    /** How far the price has moved since the program started, in percent. */
    public double getChangePercent() {
        return (getLastPrice() - openPrice) / openPrice * 100.0;
    }

    @Override
    public String toString() {
        return symbol + " (" + name + ") @ " + String.format("%.2f", getLastPrice());
    }
}
