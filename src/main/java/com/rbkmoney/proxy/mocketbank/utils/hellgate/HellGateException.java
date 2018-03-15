package com.rbkmoney.proxy.mocketbank.utils.hellgate;

/**
 * Handy class for wrapping runtime {@code Exceptions} with a root cause.
 *
 * @author Anatoly Cherkasov
 * @see #getMessage()
 * @see #printStackTrace
 */
public class HellGateException extends RuntimeException {

    /**
     * Constructs a new {@code HellGateException} with {@code null} as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     */
    public HellGateException() {
        super();
    }

    /**
     * Construct a new {@code HellGateException} with the specified detail message.
     *
     * @param message the detail message
     */
    public HellGateException(String message) {
        super(message);
    }

    /**
     * Construct a new {@code HellGateException} with the cause.
     *
     * @param cause the root cause
     */
    public HellGateException(Throwable cause) {
        super(cause);
    }

    /**
     * Construct a new {@code HellGateException} with the
     * specified detail message and root cause.
     *
     * @param message the detail message
     * @param cause   the root cause
     */
    public HellGateException(String message, Throwable cause) {
        super(message, cause);
    }

}
