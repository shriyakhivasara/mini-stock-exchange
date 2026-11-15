package engine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import exceptions.ExchangeException;
import exceptions.InsufficientFundsException;
import exceptions.InsufficientSharesException;
import exceptions.InvalidOrderException;
import model.LimitOrder;
import model.MarketOrder;
import model.Order;
import model.OrderSide;
import model.Stock;
import model.Trade;
import model.Trader;

/**
 * The heart of the exchange.
 *
 * It owns one OrderBook per stock. When an order arrives it:
 *   1. checks the order is valid (throws an exception if not),
 *   2. matches it against waiting orders on the opposite side,
 *   3. creates a Trade for each match and updates cash and shares,
 *   4. leaves any unfilled part of a limit order waiting in the book.
 */
public class MatchingEngine {

    // Money is stored as double, so two amounts that should be equal can
    // differ by a tiny fraction. We allow this much slack when comparing.
    private static final double TOLERANCE = 0.0001;

    // LinkedHashMap keeps the stocks in the order they were added.
    private final Map<String, Stock> stocks = new LinkedHashMap<>();
    private final Map<String, OrderBook> books = new LinkedHashMap<>();
    private final List<Trade> trades = new ArrayList<>();
    private final List<TradeListener> listeners = new ArrayList<>();

    // ---------------------------------------------------------------
    // Setup
    // ---------------------------------------------------------------

    /** List a new stock on the exchange and create its empty order book. */
    public void addStock(Stock stock) {
        stocks.put(stock.getSymbol(), stock);
        books.put(stock.getSymbol(), new OrderBook(stock));
    }

    public synchronized void addTradeListener(TradeListener listener) {
        listeners.add(listener);
    }

    public synchronized void removeTradeListener(TradeListener listener) {
        listeners.remove(listener);
    }

    // ---------------------------------------------------------------
    // Placing an order
    // ---------------------------------------------------------------

    /**
     * Place an order on the exchange.
     *
     * "synchronized" means only one thread can be inside this method at
     * a time. That matters later, when the bot trader places orders from
     * its own thread while the user clicks buttons in the GUI.
     *
     * @return the trades this order caused (empty if it matched nothing)
     * @throws ExchangeException if the order is rejected
     */
    public synchronized List<Trade> placeOrder(Order order) throws ExchangeException {
        validate(order);

        OrderBook book = books.get(order.getStock().getSymbol());
        List<Trade> newTrades = new ArrayList<>();

        // An incoming BUY looks at waiting SELL orders, and the reverse.
        OrderSide oppositeSide;
        if (order.getSide() == OrderSide.BUY) {
            oppositeSide = OrderSide.SELL;
        } else {
            oppositeSide = OrderSide.BUY;
        }

        // The matching loop: keep trading with the best waiting order
        // until this order is filled or no acceptable price is left.
        while (!order.isFilled()) {
            LimitOrder waiting = book.getBest(oppositeSide);
            if (waiting == null) {
                break;                      // nobody left on the other side
            }

            // The trade happens at the price of the order that was waiting.
            double price = waiting.getLimitPrice();
            if (!order.acceptsPrice(price)) {
                break;                      // best price available is not good enough
            }

            int quantity = Math.min(order.getRemainingQuantity(), waiting.getRemainingQuantity());

            // A market BUY has no price limit, so make sure the buyer
            // never spends more than the cash they have free.
            if (order instanceof MarketOrder && order.getSide() == OrderSide.BUY) {
                int affordable = (int) Math.floor((getFreeCash(order.getTrader()) + TOLERANCE) / price);
                quantity = Math.min(quantity, affordable);
                if (quantity == 0) {
                    break;
                }
            }

            Trade trade;
            if (order.getSide() == OrderSide.BUY) {
                trade = executeTrade(order, waiting, quantity, price);
            } else {
                trade = executeTrade(waiting, order, quantity, price);
            }
            newTrades.add(trade);

            if (waiting.isFilled()) {
                book.removeBest(oppositeSide);
            }
        }

        // Whatever is left of a limit order waits in the book.
        // Whatever is left of a market order is simply dropped.
        if (!order.isFilled() && order instanceof LimitOrder) {
            book.add((LimitOrder) order);
        }

        return newTrades;
    }

    /** Carry out one trade between a buy order and a sell order. */
    private Trade executeTrade(Order buyOrder, Order sellOrder, int quantity, double price) {
        Stock stock = buyOrder.getStock();
        Trader buyer = buyOrder.getTrader();
        Trader seller = sellOrder.getTrader();

        Trade trade = new Trade(stock, buyer, seller, quantity, price);

        buyOrder.fill(quantity);
        sellOrder.fill(quantity);

        buyer.deductCash(trade.getValue());
        buyer.addShares(stock.getSymbol(), quantity);
        seller.addCash(trade.getValue());
        seller.removeShares(stock.getSymbol(), quantity);

        stock.setLastPrice(price);
        trades.add(trade);

        // Tell everyone who is listening (later: the GUI and the database).
        for (TradeListener listener : listeners) {
            listener.onTrade(trade);
        }
        return trade;
    }

    // ---------------------------------------------------------------
    // Validation
    // ---------------------------------------------------------------

    /** Reject the order with an exception if anything is wrong with it. */
    private void validate(Order order) throws ExchangeException {
        String symbol = order.getStock().getSymbol();
        Trader trader = order.getTrader();

        if (!books.containsKey(symbol)) {
            throw new InvalidOrderException("Stock " + symbol + " is not listed on this exchange");
        }
        if (order.getQuantity() <= 0) {
            throw new InvalidOrderException("Quantity must be at least 1");
        }

        if (order instanceof LimitOrder) {
            double limitPrice = ((LimitOrder) order).getLimitPrice();
            if (limitPrice <= 0) {
                throw new InvalidOrderException("Price must be greater than 0");
            }
        }

        OrderBook book = books.get(symbol);

        if (order.getSide() == OrderSide.SELL) {
            // You can only sell shares you own and have not already offered.
            int freeShares = getFreeShares(trader, symbol);
            if (order.getQuantity() > freeShares) {
                throw new InsufficientSharesException(symbol, order.getQuantity(), freeShares);
            }
            if (order instanceof MarketOrder && book.getBest(OrderSide.BUY) == null) {
                throw new InvalidOrderException("No buyers for " + symbol
                        + " right now, so a market order cannot be filled");
            }
        } else {
            double freeCash = getFreeCash(trader);
            if (order instanceof LimitOrder) {
                // Worst case: the whole order trades at its limit price.
                double required = order.getQuantity() * ((LimitOrder) order).getLimitPrice();
                if (required > freeCash + TOLERANCE) {
                    throw new InsufficientFundsException(required, freeCash);
                }
            } else {
                LimitOrder bestSell = book.getBest(OrderSide.SELL);
                if (bestSell == null) {
                    throw new InvalidOrderException("No sellers for " + symbol
                            + " right now, so a market order cannot be filled");
                }
                // The buyer must be able to afford at least one share.
                if (bestSell.getLimitPrice() > freeCash + TOLERANCE) {
                    throw new InsufficientFundsException(bestSell.getLimitPrice(), freeCash);
                }
            }
        }
    }

    /**
     * Cash the trader can still spend: their cash minus the money already
     * promised to their waiting buy orders.
     */
    public synchronized double getFreeCash(Trader trader) {
        double reserved = 0;
        for (OrderBook book : books.values()) {
            for (LimitOrder waiting : book.getOrders(OrderSide.BUY)) {
                if (waiting.getTrader() == trader) {
                    reserved += waiting.getRemainingQuantity() * waiting.getLimitPrice();
                }
            }
        }
        return trader.getCash() - reserved;
    }

    /**
     * Shares of one stock the trader can still sell: what they own minus
     * the shares already offered in their waiting sell orders.
     */
    public synchronized int getFreeShares(Trader trader, String symbol) {
        int reserved = 0;
        OrderBook book = books.get(symbol);
        if (book != null) {
            for (LimitOrder waiting : book.getOrders(OrderSide.SELL)) {
                if (waiting.getTrader() == trader) {
                    reserved += waiting.getRemainingQuantity();
                }
            }
        }
        return trader.getSharesOf(symbol) - reserved;
    }

    // ---------------------------------------------------------------
    // Read-only access (used by the GUI)
    // ---------------------------------------------------------------

    public List<Stock> getStocks() {
        return new ArrayList<>(stocks.values());
    }

    /** Returns null if no stock has this symbol. */
    public Stock getStock(String symbol) {
        return stocks.get(symbol);
    }

    /** Returns null if no stock has this symbol. */
    public OrderBook getOrderBook(String symbol) {
        return books.get(symbol);
    }

    /**
     * The waiting orders for one stock and side, best first. Synchronized,
     * so the GUI gets a safe copy even while another thread is trading.
     */
    public synchronized List<LimitOrder> getWaitingOrders(String symbol, OrderSide side) {
        OrderBook book = books.get(symbol);
        if (book == null) {
            return new ArrayList<>();
        }
        return book.getOrders(side);
    }

    /** How many trades have happened since the program started. */
    public synchronized int getTradeCount() {
        return trades.size();
    }

    /** Every trade so far, oldest first. A copy, so callers cannot change history. */
    public synchronized List<Trade> getTrades() {
        return new ArrayList<>(trades);
    }
}
