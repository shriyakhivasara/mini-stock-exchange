package data;

/**
 * One row of the trades table, as read back from the database.
 *
 * This is separate from model.Trade on purpose: a Trade links to live
 * Trader and Stock objects, while a TradeRecord is only saved text and
 * numbers, which is all the history table and the CSV file need.
 */
public class TradeRecord {

    private final int id;
    private final String time;
    private final String symbol;
    private final String buyer;
    private final String seller;
    private final int quantity;
    private final double price;

    public TradeRecord(int id, String time, String symbol, String buyer, String seller,
                       int quantity, double price) {
        this.id = id;
        this.time = time;
        this.symbol = symbol;
        this.buyer = buyer;
        this.seller = seller;
        this.quantity = quantity;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public String getTime() {
        return time;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getBuyer() {
        return buyer;
    }

    public String getSeller() {
        return seller;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public double getValue() {
        return quantity * price;
    }
}
