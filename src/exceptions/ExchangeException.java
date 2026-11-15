package exceptions;

/**
 * Parent of every error the exchange can raise on purpose.
 *
 * It extends Exception (not RuntimeException), so it is a CHECKED
 * exception: any code that places an order is forced by the compiler
 * to handle it with try/catch. The GUI catches this one type to show
 * an error message for any kind of rejected order.
 */
public class ExchangeException extends Exception {

    public ExchangeException(String message) {
        super(message);
    }
}
