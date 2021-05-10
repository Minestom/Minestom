package net.minestom.server.exception;

/**
 * Used when you want to implement your own exception handling, instead of just printing the stack trace.
 * <p>
 * Sets with {@link ExceptionManager#setExceptionHandler(ExceptionHandler)}.
 */
@FunctionalInterface
public interface ExceptionHandler {

    /**
     * Called when a exception was caught.
     *
     * @param e the thrown exception
     */
    void handleException(Throwable e);
}
