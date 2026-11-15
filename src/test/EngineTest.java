package test;

import java.util.List;

import engine.MatchingEngine;
import engine.TradeListener;
import exceptions.ExchangeException;
import exceptions.InsufficientFundsException;
import exceptions.InsufficientSharesException;
import exceptions.InvalidOrderException;
import model.HumanTrader;
import model.LimitOrder;
import model.MarketOrder;
import model.Order;
import model.OrderSide;
import model.Stock;
import model.Trade;
import model.Trader;

/**
 * Checks that the matching engine follows its rules.
 *
 * Each test builds a fresh exchange, places a few orders, and compares
 * what happened with what should have happened. No testing library is
 * used: check() simply prints PASS or FAIL.
 *
 * Run with:  java -cp out test.EngineTest
 */
public class EngineTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws ExchangeException {
        testFullMatch();
        testNoMatchWhenPricesDoNotCross();
        testPartialFill();
        testBestPriceGoesFirst();
        testEarlierOrderGoesFirstAtSamePrice();
        testMarketOrderUsesSeveralPrices();
        testMarketOrderLeftoverIsDropped();
        testMarketBuyLimitedByCash();
        testListenerIsToldAboutEachTrade();
        testInvalidOrdersAreRejected();
        testNotEnoughCash();
        testNotEnoughShares();
        testWaitingOrdersReserveCashAndShares();

        System.out.println();
        System.out.println(passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private static void check(String description, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("PASS  " + description);
        } else {
            failed++;
            System.out.println("FAIL  " + description);
        }
    }

    private static boolean same(double a, double b) {
        return Math.abs(a - b) < 0.0001;
    }

    private static MatchingEngine newEngine(Stock stock) {
        MatchingEngine engine = new MatchingEngine();
        engine.addStock(stock);
        return engine;
    }

    private static Stock newStock() {
        return new Stock("TECH", "TechNova Ltd", 100.00);
    }

    /** A trader with the given cash and number of TECH shares. */
    private static Trader newTrader(String name, double cash, int shares) {
        Trader trader = new HumanTrader(name, name.toLowerCase(), "pw", cash);
        if (shares > 0) {
            trader.addShares("TECH", shares);
        }
        return trader;
    }

    // ---------------------------------------------------------------
    // Matching rules
    // ---------------------------------------------------------------

    private static void testFullMatch() throws ExchangeException {
        Stock stock = newStock();
        MatchingEngine engine = newEngine(stock);
        Trader buyer = newTrader("Buyer", 10000, 0);
        Trader seller = newTrader("Seller", 0, 50);

        engine.placeOrder(new LimitOrder(seller, stock, OrderSide.SELL, 10, 101.00));
        List<Trade> trades = engine.placeOrder(new LimitOrder(buyer, stock, OrderSide.BUY, 10, 102.00));

        check("full match: one trade is made", trades.size() == 1);
        check("full match: trade uses the waiting order's price (101)", same(trades.get(0).getPrice(), 101.00));
        check("full match: buyer pays 1010", same(buyer.getCash(), 8990.00));
        check("full match: seller receives 1010", same(seller.getCash(), 1010.00));
        check("full match: buyer now owns 10 shares", buyer.getSharesOf("TECH") == 10);
        check("full match: seller has 40 shares left", seller.getSharesOf("TECH") == 40);
        check("full match: stock price becomes 101", same(stock.getLastPrice(), 101.00));
        check("full match: order book is empty afterwards",
                engine.getOrderBook("TECH").getBest(OrderSide.BUY) == null
                        && engine.getOrderBook("TECH").getBest(OrderSide.SELL) == null);
    }

    private static void testNoMatchWhenPricesDoNotCross() throws ExchangeException {
        Stock stock = newStock();
        MatchingEngine engine = newEngine(stock);
        Trader buyer = newTrader("Buyer", 10000, 0);
        Trader seller = newTrader("Seller", 0, 50);

        engine.placeOrder(new LimitOrder(seller, stock, OrderSide.SELL, 10, 101.00));
        List<Trade> trades = engine.placeOrder(new LimitOrder(buyer, stock, OrderSide.BUY, 10, 99.00));

        check("no cross: no trade when buyer offers 99 and seller wants 101", trades.isEmpty());
        check("no cross: both orders wait in the book",
                engine.getOrderBook("TECH").getOrders(OrderSide.BUY).size() == 1
                        && engine.getOrderBook("TECH").getOrders(OrderSide.SELL).size() == 1);
        check("no cross: stock price is unchanged", same(stock.getLastPrice(), 100.00));
    }

    private static void testPartialFill() throws ExchangeException {
        Stock stock = newStock();
        MatchingEngine engine = newEngine(stock);
        Trader buyer = newTrader("Buyer", 10000, 0);
        Trader seller = newTrader("Seller", 0, 50);

        engine.placeOrder(new LimitOrder(seller, stock, OrderSide.SELL, 6, 100.00));
        Order buy = new LimitOrder(buyer, stock, OrderSide.BUY, 10, 100.00);
        List<Trade> trades = engine.placeOrder(buy);

        check("partial fill: 6 of 10 shares trade", trades.size() == 1 && trades.get(0).getQuantity() == 6);
        check("partial fill: 4 shares are still wanted", buy.getRemainingQuantity() == 4);
        check("partial fill: the rest of the buy order waits in the book",
                engine.getOrderBook("TECH").getBest(OrderSide.BUY) == buy);
    }

    private static void testBestPriceGoesFirst() throws ExchangeException {
        Stock stock = newStock();
        MatchingEngine engine = newEngine(stock);
        Trader buyer = newTrader("Buyer", 10000, 0);
        Trader expensive = newTrader("Expensive", 0, 50);
        Trader cheap = newTrader("Cheap", 0, 50);

        engine.placeOrder(new LimitOrder(expensive, stock, OrderSide.SELL, 5, 103.00));
        engine.placeOrder(new LimitOrder(cheap, stock, OrderSide.SELL, 5, 101.00));
        List<Trade> trades = engine.placeOrder(new MarketOrder(buyer, stock, OrderSide.BUY, 5));

        check("price priority: the cheaper seller trades first",
                trades.size() == 1 && trades.get(0).getSeller() == cheap && same(trades.get(0).getPrice(), 101.00));
    }

    private static void testEarlierOrderGoesFirstAtSamePrice() throws ExchangeException {
        Stock stock = newStock();
        MatchingEngine engine = newEngine(stock);
        Trader buyer = newTrader("Buyer", 10000, 0);
        Trader first = newTrader("First", 0, 50);
        Trader second = newTrader("Second", 0, 50);

        engine.placeOrder(new LimitOrder(first, stock, OrderSide.SELL, 5, 100.00));
        engine.placeOrder(new LimitOrder(second, stock, OrderSide.SELL, 5, 100.00));
        List<Trade> trades = engine.placeOrder(new MarketOrder(buyer, stock, OrderSide.BUY, 5));

        check("time priority: at the same price, the earlier seller trades first",
                trades.size() == 1 && trades.get(0).getSeller() == first);
    }

    private static void testMarketOrderUsesSeveralPrices() throws ExchangeException {
        Stock stock = newStock();
        MatchingEngine engine = newEngine(stock);
        Trader buyer = newTrader("Buyer", 10000, 0);
        Trader seller = newTrader("Seller", 0, 50);

        engine.placeOrder(new LimitOrder(seller, stock, OrderSide.SELL, 5, 100.00));
        engine.placeOrder(new LimitOrder(seller, stock, OrderSide.SELL, 5, 102.00));
        List<Trade> trades = engine.placeOrder(new MarketOrder(buyer, stock, OrderSide.BUY, 8));

        check("market sweep: two trades are made", trades.size() == 2);
        check("market sweep: 5 shares at 100, then 3 shares at 102",
                trades.size() == 2
                        && trades.get(0).getQuantity() == 5 && same(trades.get(0).getPrice(), 100.00)
                        && trades.get(1).getQuantity() == 3 && same(trades.get(1).getPrice(), 102.00));
        check("market sweep: buyer pays 500 + 306 = 806", same(buyer.getCash(), 10000 - 806));
        check("market sweep: 2 shares at 102 are still for sale",
                engine.getOrderBook("TECH").getBest(OrderSide.SELL).getRemainingQuantity() == 2);
    }

    private static void testMarketOrderLeftoverIsDropped() throws ExchangeException {
        Stock stock = newStock();
        MatchingEngine engine = newEngine(stock);
        Trader buyer = newTrader("Buyer", 10000, 0);
        Trader seller = newTrader("Seller", 0, 50);

        engine.placeOrder(new LimitOrder(seller, stock, OrderSide.SELL, 5, 100.00));
        List<Trade> trades = engine.placeOrder(new MarketOrder(buyer, stock, OrderSide.BUY, 8));

        check("market leftover: only the 5 available shares trade",
                trades.size() == 1 && trades.get(0).getQuantity() == 5);
        check("market leftover: the unfilled 3 shares do not wait in the book",
                engine.getOrderBook("TECH").getBest(OrderSide.BUY) == null);
    }

    private static void testMarketBuyLimitedByCash() throws ExchangeException {
        Stock stock = newStock();
        MatchingEngine engine = newEngine(stock);
        Trader buyer = newTrader("Buyer", 250, 0);
        Trader seller = newTrader("Seller", 0, 50);

        engine.placeOrder(new LimitOrder(seller, stock, OrderSide.SELL, 5, 100.00));
        List<Trade> trades = engine.placeOrder(new MarketOrder(buyer, stock, OrderSide.BUY, 5));

        check("market buy: with 250 cash only 2 shares at 100 are bought",
                trades.size() == 1 && trades.get(0).getQuantity() == 2);
        check("market buy: cash never goes below zero", buyer.getCash() >= 0 && same(buyer.getCash(), 50.00));
    }

    private static void testListenerIsToldAboutEachTrade() throws ExchangeException {
        Stock stock = newStock();
        MatchingEngine engine = newEngine(stock);
        Trader buyer = newTrader("Buyer", 10000, 0);
        Trader seller = newTrader("Seller", 0, 50);

        // A tiny listener that just counts how many times it was called.
        final int[] count = {0};
        engine.addTradeListener(new TradeListener() {
            @Override
            public void onTrade(Trade trade) {
                count[0]++;
            }
        });

        engine.placeOrder(new LimitOrder(seller, stock, OrderSide.SELL, 5, 100.00));
        engine.placeOrder(new LimitOrder(seller, stock, OrderSide.SELL, 5, 101.00));
        engine.placeOrder(new MarketOrder(buyer, stock, OrderSide.BUY, 10));

        check("listener: called once for each of the 2 trades", count[0] == 2);
        check("listener: engine remembers both trades", engine.getTrades().size() == 2);
    }

    // ---------------------------------------------------------------
    // Exceptions
    // ---------------------------------------------------------------

    private static void testInvalidOrdersAreRejected() {
        Stock stock = newStock();
        MatchingEngine engine = newEngine(stock);
        Trader trader = newTrader("Trader", 10000, 50);
        Stock unlisted = new Stock("FAKE", "Not Listed", 10.00);

        check("invalid: quantity 0 is rejected",
                isRejectedAsInvalid(engine, new LimitOrder(trader, stock, OrderSide.BUY, 0, 100.00)));
        check("invalid: negative quantity is rejected",
                isRejectedAsInvalid(engine, new LimitOrder(trader, stock, OrderSide.BUY, -5, 100.00)));
        check("invalid: price 0 is rejected",
                isRejectedAsInvalid(engine, new LimitOrder(trader, stock, OrderSide.BUY, 5, 0)));
        check("invalid: unlisted stock is rejected",
                isRejectedAsInvalid(engine, new LimitOrder(trader, unlisted, OrderSide.BUY, 5, 10.00)));
        check("invalid: market buy with no sellers is rejected",
                isRejectedAsInvalid(engine, new MarketOrder(trader, stock, OrderSide.BUY, 5)));
        check("invalid: market sell with no buyers is rejected",
                isRejectedAsInvalid(engine, new MarketOrder(trader, stock, OrderSide.SELL, 5)));
        check("invalid: rejected orders leave the book empty",
                engine.getOrderBook("TECH").getOrders(OrderSide.BUY).isEmpty()
                        && engine.getOrderBook("TECH").getOrders(OrderSide.SELL).isEmpty());
    }

    private static boolean isRejectedAsInvalid(MatchingEngine engine, Order order) {
        try {
            engine.placeOrder(order);
            return false;
        } catch (InvalidOrderException e) {
            return true;
        } catch (ExchangeException e) {
            return false;
        }
    }

    private static void testNotEnoughCash() {
        Stock stock = newStock();
        MatchingEngine engine = newEngine(stock);
        Trader poor = newTrader("Poor", 500, 0);

        boolean thrown = false;
        try {
            engine.placeOrder(new LimitOrder(poor, stock, OrderSide.BUY, 10, 100.00));
        } catch (InsufficientFundsException e) {
            thrown = same(e.getRequired(), 1000.00) && same(e.getAvailable(), 500.00);
        } catch (ExchangeException e) {
            thrown = false;
        }
        check("funds: buying 1000 worth with 500 cash throws InsufficientFundsException", thrown);
        check("funds: cash is untouched after the rejection", same(poor.getCash(), 500.00));
    }

    private static void testNotEnoughShares() {
        Stock stock = newStock();
        MatchingEngine engine = newEngine(stock);
        Trader trader = newTrader("Trader", 1000, 5);

        boolean thrown = false;
        try {
            engine.placeOrder(new LimitOrder(trader, stock, OrderSide.SELL, 10, 100.00));
        } catch (InsufficientSharesException e) {
            thrown = e.getRequired() == 10 && e.getAvailable() == 5;
        } catch (ExchangeException e) {
            thrown = false;
        }
        check("shares: selling 10 while owning 5 throws InsufficientSharesException", thrown);
    }

    private static void testWaitingOrdersReserveCashAndShares() throws ExchangeException {
        Stock stock = newStock();
        MatchingEngine engine = newEngine(stock);
        Trader trader = newTrader("Trader", 1000, 10);

        // First buy order promises 600 of the 1000 cash.
        engine.placeOrder(new LimitOrder(trader, stock, OrderSide.BUY, 6, 100.00));
        check("reserve: 400 cash is free after a waiting 600 buy order", same(engine.getFreeCash(trader), 400.00));

        boolean cashRejected = false;
        try {
            engine.placeOrder(new LimitOrder(trader, stock, OrderSide.BUY, 5, 100.00));
        } catch (InsufficientFundsException e) {
            cashRejected = true;
        }
        check("reserve: a second buy order needing 500 is rejected", cashRejected);

        // First sell order offers 7 of the 10 shares.
        engine.placeOrder(new LimitOrder(trader, stock, OrderSide.SELL, 7, 120.00));
        check("reserve: 3 shares are free after offering 7", engine.getFreeShares(trader, "TECH") == 3);

        boolean sharesRejected = false;
        try {
            engine.placeOrder(new LimitOrder(trader, stock, OrderSide.SELL, 4, 120.00));
        } catch (InsufficientSharesException e) {
            sharesRejected = true;
        }
        check("reserve: a second sell order for 4 shares is rejected", sharesRejected);
    }
}
