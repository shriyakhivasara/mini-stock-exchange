package exceptions;

/**
 * Thrown when an order makes no sense, for example a quantity of zero,
 * a negative price, or a stock that is not listed on the exchange.
 */
public class InvalidOrderException extends ExchangeException {

    public InvalidOrderException(String message) {
        super(message);
    }
}
