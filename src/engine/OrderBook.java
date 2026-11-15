package engine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import model.LimitOrder;
import model.OrderSide;
import model.Stock;

/**
 * The waiting orders for ONE stock.
 *
 * It keeps two priority queues:
 *   - buy orders, best first  = HIGHEST price first
 *   - sell orders, best first = LOWEST price first
 * When two orders have the same price, the one placed earlier wins.
 * This rule is called "price-time priority".
 *
 * Only limit orders wait here. Market orders never wait.
 */
public class OrderBook {

    /** Sorts buy orders: highest price first, then earliest first. */
    private static class BuyComparator implements Comparator<LimitOrder> {
        @Override
        public int compare(LimitOrder a, LimitOrder b) {
            if (a.getLimitPrice() != b.getLimitPrice()) {
                return Double.compare(b.getLimitPrice(), a.getLimitPrice());
            }
            return Long.compare(a.getSequence(), b.getSequence());
        }
    }

    /** Sorts sell orders: lowest price first, then earliest first. */
    private static class SellComparator implements Comparator<LimitOrder> {
        @Override
        public int compare(LimitOrder a, LimitOrder b) {
            if (a.getLimitPrice() != b.getLimitPrice()) {
                return Double.compare(a.getLimitPrice(), b.getLimitPrice());
            }
            return Long.compare(a.getSequence(), b.getSequence());
        }
    }

    private final Stock stock;
    private final PriorityQueue<LimitOrder> buyOrders = new PriorityQueue<>(new BuyComparator());
    private final PriorityQueue<LimitOrder> sellOrders = new PriorityQueue<>(new SellComparator());

    public OrderBook(Stock stock) {
        this.stock = stock;
    }

    public Stock getStock() {
        return stock;
    }

    /** Put a limit order into the correct queue to wait. */
    public void add(LimitOrder order) {
        if (order.getSide() == OrderSide.BUY) {
            buyOrders.add(order);
        } else {
            sellOrders.add(order);
        }
    }

    /** The best waiting order on one side, or null if that side is empty. */
    public LimitOrder getBest(OrderSide side) {
        if (side == OrderSide.BUY) {
            return buyOrders.peek();
        } else {
            return sellOrders.peek();
        }
    }

    /** Remove the best waiting order on one side (used once it is filled). */
    public void removeBest(OrderSide side) {
        if (side == OrderSide.BUY) {
            buyOrders.poll();
        } else {
            sellOrders.poll();
        }
    }

    /**
     * All waiting orders on one side, sorted best first.
     * Returns a new list, so the caller cannot disturb the real queue.
     * Used by the GUI table and by the engine's validation.
     */
    public List<LimitOrder> getOrders(OrderSide side) {
        List<LimitOrder> list;
        if (side == OrderSide.BUY) {
            list = new ArrayList<>(buyOrders);
            list.sort(new BuyComparator());
        } else {
            list = new ArrayList<>(sellOrders);
            list.sort(new SellComparator());
        }
        return list;
    }
}
