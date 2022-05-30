package net.minestom.server.event.trait;

import net.minestom.server.permission.PermissionHandler;

/**
 * Represents an event called on a {@link PermissionHandler}
 */
public interface PermissionEvent extends CancellableEvent {
    /**
     * Returns the {@link PermissionHandler} associated to this event.
     *
     * @return the {@link PermissionHandler} associated to this event.
     */
    PermissionHandler getPermissionHandler();

    /**
     * Returns the result of the event.
     *
     * @return the result of the event.
     */
    Result getResult();

    /**
     * Sets the result of the event.
     *
     * @param result the result of the event.
     */
    void setResult(Result result);

    @Override
    default boolean isCancelled() {
        return getResult() == Result.DENY;
    }

    @Override
    default void setCancelled(boolean cancel) {
        setResult(cancel ? Result.DENY : Result.DEFAULT);
    }

    /**
     * Represents the result of a {@link PermissionEvent}.
     */
    enum Result {
        /**
         * The call is successful.
         */
        ALLOW,
        /**
         * The call is denied.
         */
        DENY,
        /**
         * The call is not successful and the default behavior should be used.
         */
        DEFAULT
    }
}
