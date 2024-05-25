package net.minestom.server.exception;

import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the handling of exceptions.
 */
public final class ExceptionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionManager.class);

    private ExceptionHandler exceptionHandler;

    /**
     * Handles an exception, if no {@link ExceptionHandler} is set, it just prints the stack trace.
     *
     * @param e the occurred exception
     */
    public void handleException(Throwable e) {
        if (e instanceof OutOfMemoryError) {
            // OOM should be handled manually
            LOGGER.error("OOM error", e);
            MinecraftServer.stopCleanly();
            return;
        }
        this.getExceptionHandler().handleException(e);
    }

    /**
     * Changes the exception handler, to allow custom exception handling.
     *
     * @param exceptionHandler the new {@link ExceptionHandler}, can be set to null to apply the default provider
     */
    public void setExceptionHandler(@Nullable ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * Retrieves the current {@link ExceptionHandler}, can be the default one if none is defined.
     *
     * @return the current {@link ExceptionHandler}
     */
    public ExceptionHandler getExceptionHandler() {
        return this.exceptionHandler == null ? exceptionHandler = Throwable::printStackTrace : this.exceptionHandler;
    }
}
