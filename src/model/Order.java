package model;

/**
 * A request to buy or sell some shares of one stock.
 *
 * This class is ABSTRACT. The two real kinds of order are:
 *   - LimitOrder  : "buy/sell, but only at this price or better"
 *   - MarketOrder : "buy/sell right now at the best available price"
 *
 * Everything the two kinds have in common lives here. The one thing
 * that differs (which prices they accept) is the abstract method
 * acceptsPrice(), which each subclass answers in its own way.
 */
public abstract class Order {

    private static int nextId = 1;
    private static long nextSequence = 1;

    private final int id;
    private final Trader trader;
    private final Stock stock;
    private final OrderSide side;
    private final int quantity;        // what was originally asked for
    private int remainingQuantity;     // what is still waiting to be traded
    private final long sequence;       // arrival order: smaller = placed earlier

    protected Order(Trader trader, Stock stock, OrderSide side, int quantity) {
        this.id = nextId++;
        this.sequence = nextSequence++;
        this.trader = trader;
        this.stock = stock;
        this.side = side;
        this.quantity = quantity;
        this.remainingQuantity = quantity;
    }

    /** Would this order be willing to trade at the given price? */
    public abstract boolean acceptsPrice(double price);

    /** "LIMIT" or "MARKET", used when displaying the order. */
    public abstract String getTypeName();

    /**
     * Record that part (or all) of this order has been traded.
     * Example: an order for 10 shares that trades 6 has 4 remaining.
     */
    public void fill(int tradedQuantity) {
        remainingQuantity -= tradedQuantity;
    }

    public boolean isFilled() {
        return remainingQuantity == 0;
    }

    public int getId() {
        return id;
    }

    public Trader getTrader() {
        return trader;
    }

    public Stock getStock() {
        return stock;
    }

    public OrderSide getSide() {
        return side;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getRemainingQuantity() {
        return remainingQuantity;
    }

    public long getSequence() {
        return sequence;
    }

    @Override
    public String toString() {
        return "Order #" + id + " " + getTypeName() + " " + side + " "
                + remainingQuantity + "/" + quantity + " " + stock.getSymbol()
                + " by " + trader.getName();
    }
}
