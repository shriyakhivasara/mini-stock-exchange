package test;

import java.util.List;

import bot.BotTrader;
import engine.MatchingEngine;
import model.LimitOrder;
import model.OrderSide;
import model.Stock;

/**
 * Runs two bots at high speed on their own threads for a few seconds,
 * then checks that the exchange is still consistent: no cash or shares
 * were created or lost, and nobody spent money they did not have.
 * This is the test that would catch a multithreading bug.
 *
 * Run with:  java -cp out test.BotTest
 */
public class BotTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws InterruptedException {
        MatchingEngine engine = new MatchingEngine();
        engine.addStock(new Stock("TECH", "TechNova Ltd", 100.00));
        engine.addStock(new Stock("FOOD", "FoodBazaar Inc", 25.00));

        // A 2 millisecond delay makes the bots trade hundreds of times a second.
        BotTrader alpha = new BotTrader("Bot Alpha", engine, 2);
        BotTrader beta = new BotTrader("Bot Beta", engine, 2);

        double cashBefore = alpha.getCash() + beta.getCash();
        int techBefore = alpha.getSharesOf("TECH") + beta.getSharesOf("TECH");
        int foodBefore = alpha.getSharesOf("FOOD") + beta.getSharesOf("FOOD");

        Thread alphaThread = new Thread(alpha, "Bot Alpha");
        Thread betaThread = new Thread(beta, "Bot Beta");
        alphaThread.start();
        betaThread.start();

        Thread.sleep(3000);                 // let them trade for 3 seconds

        alpha.stop();
        beta.stop();
        alphaThread.join();                 // wait until both threads have finished
        betaThread.join();

        int trades = engine.getTradeCount();
        System.out.println("The bots made " + trades + " trades in 3 seconds.");
        System.out.println("TECH is now " + engine.getStock("TECH").getLastPrice()
                + ", FOOD is now " + engine.getStock("FOOD").getLastPrice());

        check("bots: trades happened", trades > 0);
        check("bots: total cash is unchanged",
                Math.abs(alpha.getCash() + beta.getCash() - cashBefore) < 0.01);
        check("bots: total TECH shares are unchanged",
                alpha.getSharesOf("TECH") + beta.getSharesOf("TECH") == techBefore);
        check("bots: total FOOD shares are unchanged",
                alpha.getSharesOf("FOOD") + beta.getSharesOf("FOOD") == foodBefore);
        check("bots: nobody has promised more cash than they have",
                engine.getFreeCash(alpha) >= -0.01 && engine.getFreeCash(beta) >= -0.01);
        check("bots: nobody has offered more shares than they have",
                engine.getFreeShares(alpha, "TECH") >= 0 && engine.getFreeShares(beta, "TECH") >= 0
                        && engine.getFreeShares(alpha, "FOOD") >= 0 && engine.getFreeShares(beta, "FOOD") >= 0);
        check("bots: best TECH buy price is below best TECH sell price", bookIsNotCrossed(engine, "TECH"));
        check("bots: best FOOD buy price is below best FOOD sell price", bookIsNotCrossed(engine, "FOOD"));
        check("bots: prices stay positive",
                engine.getStock("TECH").getLastPrice() > 0 && engine.getStock("FOOD").getLastPrice() > 0);

        System.out.println();
        System.out.println(passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    /** If the best buyer would pay at least what the best seller asks, a trade was missed. */
    private static boolean bookIsNotCrossed(MatchingEngine engine, String symbol) {
        List<LimitOrder> buys = engine.getWaitingOrders(symbol, OrderSide.BUY);
        List<LimitOrder> sells = engine.getWaitingOrders(symbol, OrderSide.SELL);
        if (buys.isEmpty() || sells.isEmpty()) {
            return true;
        }
        return buys.get(0).getLimitPrice() < sells.get(0).getLimitPrice();
    }

    private static void check(String description, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("PASS  " + description);
        } else {
            failed++;
            System.out.println("FAIL  " + description);
        }
    }
}
