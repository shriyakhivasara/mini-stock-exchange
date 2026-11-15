package data;

/**
 * One user's buying and selling of ONE stock, added up.
 *
 * ProfitCalculator feeds the user's trades in, oldest first, through
 * addPurchase() and addSale(). The getters then answer: how much was
 * bought and at what average price, how much was sold and at what
 * average price, and how much profit the sales made.
 *
 * Profit uses the "average cost" method: every share currently held is
 * treated as having cost the average price paid for the shares held.
 */
public class StockProfit {

    private final String symbol;

    private int sharesBought;
    private double moneySpent;        // total paid for all purchases
    private int sharesSold;
    private double moneyReceived;     // total received from all sales

    private int sharesHeld;           // bought and not yet sold
    private double costOfSharesHeld;  // what the shares still held cost
    private double costOfSharesSold;  // what the shares sold had cost
    private double profit;            // money received minus cost, for shares sold

    public StockProfit(String symbol) {
        this.symbol = symbol;
    }

    public void addPurchase(int quantity, double price) {
        sharesBought += quantity;
        moneySpent += quantity * price;

        sharesHeld += quantity;
        costOfSharesHeld += quantity * price;
    }

    public void addSale(int quantity, double price) {
        sharesSold += quantity;
        moneyReceived += quantity * price;

        // What did the shares being sold cost, on average?
        double averageCost = 0;
        if (sharesHeld > 0) {
            averageCost = costOfSharesHeld / sharesHeld;
        }
        double costOfThisSale = quantity * averageCost;

        profit += quantity * price - costOfThisSale;
        costOfSharesSold += costOfThisSale;

        sharesHeld -= quantity;
        costOfSharesHeld -= costOfThisSale;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getSharesBought() {
        return sharesBought;
    }

    public int getSharesSold() {
        return sharesSold;
    }

    public int getSharesHeld() {
        return sharesHeld;
    }

    /** Average price paid per share, or 0 if nothing was bought. */
    public double getAverageBuyPrice() {
        if (sharesBought == 0) {
            return 0;
        }
        return moneySpent / sharesBought;
    }

    /** Average price received per share, or 0 if nothing was sold. */
    public double getAverageSellPrice() {
        if (sharesSold == 0) {
            return 0;
        }
        return moneyReceived / sharesSold;
    }

    /** Profit (or loss, if negative) made on the shares sold so far. */
    public double getProfit() {
        return profit;
    }

    /** What the shares sold so far had cost. Used to total up the margin. */
    public double getCostOfSharesSold() {
        return costOfSharesSold;
    }

    /**
     * Profit as a percentage of what the sold shares cost.
     * Example: shares that cost 1,000 and sold for 1,100 give a 10% margin.
     */
    public double getProfitMarginPercent() {
        if (costOfSharesSold == 0) {
            return 0;
        }
        return profit / costOfSharesSold * 100.0;
    }
}
