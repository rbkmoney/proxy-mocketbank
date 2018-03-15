package com.rbkmoney.proxy.mocketbank.utils.cds;

/**
 * Handy class for wrapping runtime {@code Exceptions} with a root cause.
 *
 * @author Anatoly Cherkasov
 * @see #getMessage()
 * @see #printStackTrace
 */
public class CdsException extends RuntimeException {

    /**
     * Constructs a new {@code CdsException} with {@code null} as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     */
    public CdsException() {
        super();
    }

    /**
     * Construct a new {@code CdsException} with the specified detail message.
     *
     * @param message the detail message
     */
    public CdsException(String message) {
        super(message);
    }

    /**
     * Construct a new {@code CdsException} with the cause.
     *
     * @param cause the root cause
     */
    public CdsException(Throwable cause) {
        super(cause);
    }

    /**
     * Construct a new {@code CdsException} with the
     * specified detail message and root cause.
     *
     * @param message the detail message
     * @param cause   the root cause
     */
    public CdsException(String message, Throwable cause) {
        super(message, cause);
    }

}
