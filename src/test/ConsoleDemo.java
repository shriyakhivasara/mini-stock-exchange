package test;

import java.util.List;

import engine.MatchingEngine;
import engine.OrderBook;
import engine.TradeListener;
import exceptions.ExchangeException;
import model.HumanTrader;
import model.LimitOrder;
import model.MarketOrder;
import model.Order;
import model.OrderSide;
import model.Stock;
import model.Trade;
import model.Trader;

/**
 * Console demo of the matching engine, with no GUI and no database.
 *
 * It lists four stocks, places a series of orders on one of them, and
 * prints what the engine does with each. Useful for understanding the
 * matching rules and for screenshots in the report.
 *
 * Run with:  java -cp out test.ConsoleDemo
 */
public class ConsoleDemo {

    private static MatchingEngine engine;

    public static void main(String[] args) {
        engine = new MatchingEngine();

        // The four fictional stocks on our exchange.
        Stock tech = new Stock("TECH", "TechNova Ltd", 100.00);
        engine.addStock(tech);
        engine.addStock(new Stock("GRNE", "GreenEnergy Corp", 50.00));
        engine.addStock(new Stock("MEDI", "MediCare Labs", 200.00));
        engine.addStock(new Stock("FOOD", "FoodBazaar Inc", 25.00));

        // Print a line every time a trade happens (TradeListener interface).
        engine.addTradeListener(new TradeListener() {
            @Override
            public void onTrade(Trade trade) {
                System.out.println("    TRADE -> " + trade);
            }
        });

        Trader asha = new HumanTrader("Asha", "asha", "pass123", 10000.00);
        Trader ravi = new HumanTrader("Ravi", "ravi", "pass456", 10000.00);
        ravi.addShares("TECH", 50);

        System.out.println("=== Listed stocks ===");
        for (Stock stock : engine.getStocks()) {
            System.out.println(stock);
        }

        System.out.println();
        System.out.println("=== 1. Ravi offers shares at two prices; nothing trades yet ===");
        place(new LimitOrder(ravi, tech, OrderSide.SELL, 10, 101.00));
        place(new LimitOrder(ravi, tech, OrderSide.SELL, 10, 103.00));
        printBook("TECH");

        System.out.println();
        System.out.println("=== 2. Asha bids 99: too low, so her order waits ===");
        place(new LimitOrder(asha, tech, OrderSide.BUY, 5, 99.00));
        printBook("TECH");

        System.out.println();
        System.out.println("=== 3. Asha bids 102 for 15: 10 trade at 101, 5 wait ===");
        place(new LimitOrder(asha, tech, OrderSide.BUY, 15, 102.00));
        printBook("TECH");

        System.out.println();
        System.out.println("=== 4. Ravi sells 8 at market: 5 go to the 102 bid, 3 to the 99 bid ===");
        place(new MarketOrder(ravi, tech, OrderSide.SELL, 8));
        printBook("TECH");

        System.out.println();
        System.out.println("=== 5. Orders the exchange refuses ===");
        place(new LimitOrder(asha, tech, OrderSide.BUY, 0, 100.00));
        place(new LimitOrder(asha, tech, OrderSide.BUY, 1000, 100.00));
        place(new LimitOrder(asha, tech, OrderSide.SELL, 500, 100.00));
        place(new MarketOrder(asha, engine.getStock("FOOD"), OrderSide.BUY, 5));

        System.out.println();
        System.out.println("=== Final state ===");
        System.out.println(tech);
        System.out.println(asha);
        System.out.println(ravi);
        System.out.println("Trades made: " + engine.getTrades().size());
    }

    /** Send one order to the engine and report what happened. */
    private static void place(Order order) {
        System.out.println("  " + order);
        try {
            List<Trade> trades = engine.placeOrder(order);
            if (trades.isEmpty()) {
                System.out.println("    no match, order is waiting");
            }
        } catch (ExchangeException e) {
            // One catch handles every kind of rejection, because all our
            // custom exceptions extend ExchangeException.
            System.out.println("    REJECTED (" + e.getClass().getSimpleName() + "): " + e.getMessage());
        }
    }

    private static void printBook(String symbol) {
        OrderBook book = engine.getOrderBook(symbol);
        System.out.println("  Order book for " + symbol + ":");
        System.out.println("    SELL side (lowest first):  " + describe(book.getOrders(OrderSide.SELL)));
        System.out.println("    BUY side (highest first):  " + describe(book.getOrders(OrderSide.BUY)));
    }

    private static String describe(List<LimitOrder> orders) {
        if (orders.isEmpty()) {
            return "empty";
        }
        String text = "";
        for (LimitOrder order : orders) {
            if (!text.isEmpty()) {
                text += ", ";
            }
            text += order.getRemainingQuantity() + " @ " + String.format("%.2f", order.getLimitPrice());
        }
        return text;
    }
}
