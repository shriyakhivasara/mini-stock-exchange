package model;

/**
 * A real user who logs in through the GUI and places orders by hand.
 */
public class HumanTrader extends Trader {

    private final String username;
    private final String password;

    public HumanTrader(String name, String username, String password, double startingCash) {
        super(name, startingCash);   // Trader's constructor sets name and cash
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    /** Used by the login screen. */
    public boolean checkPassword(String attempt) {
        return password.equals(attempt);
    }

    @Override
    public String getTraderType() {
        return "Human";
    }
}
