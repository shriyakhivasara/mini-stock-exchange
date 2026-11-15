package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A completed deal: one buyer and one seller exchanged some shares
 * at one price. Created by the matching engine, never by hand.
 *
 * All fields are final because a trade that has happened cannot change.
 */
public class Trade {

    private static int nextId = 1;
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final int id;
    private final Stock stock;
    private final Trader buyer;
    private final Trader seller;
    private final int quantity;
    private final double price;
    private final LocalDateTime time;

    public Trade(Stock stock, Trader buyer, Trader seller, int quantity, double price) {
        this.id = nextId++;
        this.stock = stock;
        this.buyer = buyer;
        this.seller = seller;
        this.quantity = quantity;
        this.price = price;
        this.time = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public Stock getStock() {
        return stock;
    }

    public Trader getBuyer() {
        return buyer;
    }

    public Trader getSeller() {
        return seller;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public String getFormattedTime() {
        return time.format(TIME_FORMAT);
    }

    /** Total money that changed hands: shares x price. */
    public double getValue() {
        return quantity * price;
    }

    @Override
    public String toString() {
        return "Trade #" + id + ": " + buyer.getName() + " bought " + quantity + " "
                + stock.getSymbol() + " from " + seller.getName()
                + " @ " + String.format("%.2f", price);
    }
}
