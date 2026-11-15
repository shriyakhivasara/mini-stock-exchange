package model;

/**
 * An order with a price limit.
 *
 * BUY  limit 100 = "I will pay 100 or LESS per share."
 * SELL limit 100 = "I want 100 or MORE per share."
 *
 * If nobody is offering an acceptable price, the order waits in the
 * order book until someone does.
 */
public class LimitOrder extends Order {

    private final double limitPrice;

    public LimitOrder(Trader trader, Stock stock, OrderSide side, int quantity, double limitPrice) {
        super(trader, stock, side, quantity);
        this.limitPrice = limitPrice;
    }

    public double getLimitPrice() {
        return limitPrice;
    }

    @Override
    public boolean acceptsPrice(double price) {
        if (getSide() == OrderSide.BUY) {
            return price <= limitPrice;   // a buyer wants a low price
        } else {
            return price >= limitPrice;   // a seller wants a high price
        }
    }

    @Override
    public String getTypeName() {
        return "LIMIT";
    }

    @Override
    public String toString() {
        return super.toString() + " @ " + String.format("%.2f", limitPrice);
    }
}
