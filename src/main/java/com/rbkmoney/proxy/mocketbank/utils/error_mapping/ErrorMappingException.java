package com.rbkmoney.proxy.mocketbank.utils.error_mapping;

/**
 * Handy class for wrapping runtime {@code Exceptions} with a root cause.
 *
 * @author Anatoly Cherkasov
 * @see #getMessage()
 * @see #printStackTrace
 */
public class ErrorMappingException extends RuntimeException {

    /**
     * Constructs a new {@code ErrorMappingException} with {@code null} as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     */
    public ErrorMappingException() {
        super();
    }

    /**
     * Construct a new {@code ErrorMappingException} with the specified detail message.
     *
     * @param message the detail message
     */
    public ErrorMappingException(String message) {
        super(message);
    }

    /**
     * Construct a new {@code ErrorMappingException} with the cause.
     *
     * @param cause the root cause
     */
    public ErrorMappingException(Throwable cause) {
        super(cause);
    }

    /**
     * Construct a new {@code ErrorMappingException} with the
     * specified detail message and root cause.
     *
     * @param message the detail message
     * @param cause   the root cause
     */
    public ErrorMappingException(String message, Throwable cause) {
        super(message, cause);
    }

}
