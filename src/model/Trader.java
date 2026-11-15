package model;

import java.util.HashMap;
import java.util.Map;

/**
 * Anyone who can place orders on the exchange.
 *
 * This class is ABSTRACT: you cannot create a plain "Trader".
 * You create a HumanTrader (a logged-in user) or a BotTrader.
 * Both share the cash and holdings logic written here (inheritance).
 *
 * The cash and holdings methods are synchronized: a bot's trade can
 * change a trader's account on the bot thread at the same moment the
 * GUI thread is reading it to draw the portfolio.
 */
public abstract class Trader {

    private static int nextId = 1;

    private final int id;
    private final String name;
    private double cash;

    // How many shares of each stock this trader owns.
    // Key = stock symbol (e.g. "TECH"), value = number of shares.
    private final Map<String, Integer> holdings = new HashMap<>();

    protected Trader(String name, double startingCash) {
        this.id = nextId++;
        this.name = name;
        this.cash = startingCash;
    }

    /** Each kind of trader describes itself differently (polymorphism). */
    public abstract String getTraderType();

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public synchronized double getCash() {
        return cash;
    }

    public synchronized void addCash(double amount) {
        cash += amount;
    }

    public synchronized void deductCash(double amount) {
        cash -= amount;
    }

    /** Returns 0 if the trader owns none of this stock. */
    public synchronized int getSharesOf(String symbol) {
        return holdings.getOrDefault(symbol, 0);
    }

    public synchronized void addShares(String symbol, int quantity) {
        holdings.put(symbol, getSharesOf(symbol) + quantity);
    }

    public synchronized void removeShares(String symbol, int quantity) {
        int remaining = getSharesOf(symbol) - quantity;
        if (remaining == 0) {
            holdings.remove(symbol);
        } else {
            holdings.put(symbol, remaining);
        }
    }

    /** A copy, so code outside this class cannot change holdings directly. */
    public synchronized Map<String, Integer> getHoldings() {
        return new HashMap<>(holdings);
    }

    @Override
    public String toString() {
        return getTraderType() + " #" + id + " " + name
                + " | cash " + String.format("%.2f", getCash())
                + " | holdings " + getHoldings();
    }
}
