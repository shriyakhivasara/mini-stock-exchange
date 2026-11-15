package exceptions;

/**
 * Thrown when a trader tries to sell more shares than they have free.
 * (There is no short selling on this exchange.)
 */
public class InsufficientSharesException extends ExchangeException {

    private final String symbol;
    private final int required;
    private final int available;

    public InsufficientSharesException(String symbol, int required, int available) {
        super("Not enough " + symbol + " shares: need " + required
                + " but only " + available + " are free");
        this.symbol = symbol;
        this.required = required;
        this.available = available;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getRequired() {
        return required;
    }

    public int getAvailable() {
        return available;
    }
}
