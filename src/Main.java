import model.HumanTrader;
import model.LimitOrder;
import model.MarketOrder;
import model.Order;
import model.OrderSide;
import model.Stock;
import model.Trade;
import model.Trader;

/**
 * Stage 1 demo: creates a few model objects and prints them.
 * There is no matching engine yet; this only proves the classes work.
 * It will be replaced by the real program start-up in a later stage.
 */
public class Main {

    public static void main(String[] args) {
        Stock tech = new Stock("TECH", "TechNova Ltd", 100.00);

        Trader asha = new HumanTrader("Asha", "asha", "pass123", 10000.00);
        Trader ravi = new HumanTrader("Ravi", "ravi", "pass456", 10000.00);
        ravi.addShares("TECH", 50);

        System.out.println("--- Stock and traders ---");
        System.out.println(tech);
        System.out.println(asha);
        System.out.println(ravi);

        // Both variables have the type Order, but hold different subclasses.
        Order buy = new LimitOrder(asha, tech, OrderSide.BUY, 10, 102.00);
        Order sell = new MarketOrder(ravi, tech, OrderSide.SELL, 10);

        System.out.println();
        System.out.println("--- Orders ---");
        System.out.println(buy);
        System.out.println(sell);

        // Polymorphism: the same method call gives different answers.
        System.out.println();
        System.out.println("--- Would each order trade at 101.00? At 103.00? ---");
        System.out.println("Limit buy at 101.00:  " + buy.acceptsPrice(101.00));
        System.out.println("Limit buy at 103.00:  " + buy.acceptsPrice(103.00));
        System.out.println("Market sell at 101.00: " + sell.acceptsPrice(101.00));
        System.out.println("Market sell at 103.00: " + sell.acceptsPrice(103.00));

        // Do by hand what the matching engine will do automatically in stage 2.
        double price = 101.00;
        int quantity = 10;
        Trade trade = new Trade(tech, asha, ravi, quantity, price);
        buy.fill(quantity);
        sell.fill(quantity);
        asha.deductCash(trade.getValue());
        asha.addShares("TECH", quantity);
        ravi.addCash(trade.getValue());
        ravi.removeShares("TECH", quantity);
        tech.setLastPrice(price);

        System.out.println();
        System.out.println("--- After one hand-made trade ---");
        System.out.println(trade);
        System.out.println(buy + " | filled: " + buy.isFilled());
        System.out.println(tech);
        System.out.println(asha);
        System.out.println(ravi);
    }
}
