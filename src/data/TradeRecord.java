package data;

/**
 * One row of the trades table, as read back from the database.
 *
 * This is separate from model.Trade on purpose: a Trade links to live
 * Trader and Stock objects, while a TradeRecord is only saved text and
 * numbers, which is all the history tables and the CSV file need.
 */
public class TradeRecord {

    private final int id;
    private final String time;
    private final String symbol;
    private final String buyer;             // display name, e.g. "Asha" or "Bot Alpha"
    private final String seller;
    private final String buyerUsername;     // login name; empty for a bot
    private final String sellerUsername;
    private final int quantity;
    private final double price;

    public TradeRecord(int id, String time, String symbol, String buyer, String seller,
                       String buyerUsername, String sellerUsername, int quantity, double price) {
        this.id = id;
        this.time = time;
        this.symbol = symbol;
        this.buyer = buyer;
        this.seller = seller;
        this.buyerUsername = buyerUsername;
        this.sellerUsername = sellerUsername;
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

    /** Was this user the buyer in this trade? */
    public boolean wasBoughtBy(String username) {
        return buyerUsername.equals(username);
    }

    /** Was this user the seller in this trade? */
    public boolean wasSoldBy(String username) {
        return sellerUsername.equals(username);
    }
}
