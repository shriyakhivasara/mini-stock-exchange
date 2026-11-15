package model;

/**
 * An order with no price limit: "trade now at the best price available."
 *
 * A market order never waits in the order book. It takes whatever the
 * other side is offering, so it accepts every price.
 */
public class MarketOrder extends Order {

    public MarketOrder(Trader trader, Stock stock, OrderSide side, int quantity) {
        super(trader, stock, side, quantity);
    }

    @Override
    public boolean acceptsPrice(double price) {
        return true;
    }

    @Override
    public String getTypeName() {
        return "MARKET";
    }
}
