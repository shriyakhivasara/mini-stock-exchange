package exceptions;

/**
 * Thrown when a trader tries to buy more than their free cash can pay for.
 * It carries the two amounts so the GUI can show a helpful message.
 */
public class InsufficientFundsException extends ExchangeException {

    private final double required;
    private final double available;

    public InsufficientFundsException(double required, double available) {
        super("Not enough cash: need " + String.format("%.2f", required)
                + " but only " + String.format("%.2f", available) + " is free");
        this.required = required;
        this.available = available;
    }

    public double getRequired() {
        return required;
    }

    public double getAvailable() {
        return available;
    }
}
