package bot;

import java.util.List;
import java.util.Random;

import engine.MatchingEngine;
import exceptions.ExchangeException;
import model.LimitOrder;
import model.MarketOrder;
import model.OrderSide;
import model.Stock;
import model.Trader;

/**
 * A computer-controlled trader that keeps the market alive.
 *
 * It is a Trader (inheritance) that can also be run on its own thread
 * (the Runnable interface). Java allows only one parent class, so the
 * bot cannot extend both Trader and Thread; implementing Runnable is
 * how a class that already has a parent becomes runnable.
 *
 * Every second or so it picks a random stock and:
 *   1. makes sure it has a few buy orders just below the current price
 *      and a few sell orders just above it, so others can always trade;
 *   2. sometimes places a small market order, which is what moves the price.
 */
public class BotTrader extends Trader implements Runnable {

    private static final double STARTING_CASH = 1000000.00;
    private static final int STARTING_SHARES = 10000;

    private static final int ORDERS_PER_SIDE = 3;       // waiting orders to keep near the price
    private static final double NEAR_PRICE = 0.03;      // "near" means within 3% of the price
    private static final double MARKET_ORDER_CHANCE = 0.6;

    private final MatchingEngine engine;
    private final int delayMillis;
    private final Random random = new Random();

    // "volatile" makes sure the bot thread sees the change when another
    // thread calls stop().
    private volatile boolean running = true;

    /**
     * @param delayMillis the shortest pause between two actions; the real
     *                    pause is random, between one and two times this
     */
    public BotTrader(String name, MatchingEngine engine, int delayMillis) {
        super(name, STARTING_CASH);
        this.engine = engine;
        this.delayMillis = delayMillis;

        // Give the bot shares of everything, so it has something to sell.
        for (Stock stock : engine.getStocks()) {
            addShares(stock.getSymbol(), STARTING_SHARES);
        }
    }

    @Override
    public String getTraderType() {
        return "Bot";
    }

    /** The code the bot's thread runs: pause, act, repeat until stopped. */
    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(delayMillis + random.nextInt(delayMillis));
            } catch (InterruptedException e) {
                break;                      // the program is shutting down
            }
            act();
        }
    }

    /** Ask the bot to finish. Its loop ends after the current pause. */
    public void stop() {
        running = false;
    }

    /** One action on one randomly chosen stock. */
    private void act() {
        List<Stock> stocks = engine.getStocks();
        Stock stock = stocks.get(random.nextInt(stocks.size()));

        try {
            keepOrdersNearPrice(stock);
            if (random.nextDouble() < MARKET_ORDER_CHANCE) {
                placeMarketOrder(stock);
            }
        } catch (ExchangeException e) {
            // The engine refused an order (for example the bot ran low on
            // one stock). That is fine: skip this turn and try again later.
        }
    }

    /** Top up the bot's waiting orders on both sides of the current price. */
    private void keepOrdersNearPrice(Stock stock) throws ExchangeException {
        double price = stock.getLastPrice();

        if (countOwnOrdersNear(stock, OrderSide.SELL, price) < ORDERS_PER_SIDE) {
            // Ask for a little more than the current price (0.2% to 1.5% above).
            double sellPrice = roundToCents(price * (1.002 + random.nextDouble() * 0.013));
            sellPrice = Math.max(sellPrice, price + 0.01);
            engine.placeOrder(new LimitOrder(this, stock, OrderSide.SELL, randomQuantity(5, 25), sellPrice));
        }

        if (countOwnOrdersNear(stock, OrderSide.BUY, price) < ORDERS_PER_SIDE) {
            // Offer a little less than the current price (0.2% to 1.5% below).
            double buyPrice = roundToCents(price * (0.998 - random.nextDouble() * 0.013));
            buyPrice = Math.min(buyPrice, price - 0.01);
            if (buyPrice > 0) {
                engine.placeOrder(new LimitOrder(this, stock, OrderSide.BUY, randomQuantity(5, 25), buyPrice));
            }
        }
    }

    private int countOwnOrdersNear(Stock stock, OrderSide side, double price) {
        int count = 0;
        for (LimitOrder order : engine.getWaitingOrders(stock.getSymbol(), side)) {
            boolean isNear = Math.abs(order.getLimitPrice() - price) <= price * NEAR_PRICE;
            if (order.getTrader() == this && isNear) {
                count++;
            }
        }
        return count;
    }

    /** Buy or sell a few shares at whatever price is on offer. */
    private void placeMarketOrder(Stock stock) throws ExchangeException {
        OrderSide side;
        OrderSide oppositeSide;
        if (random.nextBoolean()) {
            side = OrderSide.BUY;
            oppositeSide = OrderSide.SELL;
        } else {
            side = OrderSide.SELL;
            oppositeSide = OrderSide.BUY;
        }

        List<LimitOrder> waiting = engine.getWaitingOrders(stock.getSymbol(), oppositeSide);
        if (waiting.isEmpty()) {
            return;                         // nobody to trade with
        }
        if (waiting.get(0).getTrader() == this) {
            return;                         // would only trade with itself
        }
        engine.placeOrder(new MarketOrder(this, stock, side, randomQuantity(1, 10)));
    }

    /** A random whole number from low to high, both included. */
    private int randomQuantity(int low, int high) {
        return low + random.nextInt(high - low + 1);
    }

    private double roundToCents(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
