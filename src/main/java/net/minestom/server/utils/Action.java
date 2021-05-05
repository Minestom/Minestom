package net.minestom.server.utils;

/**
 * A functional interface to perform an action.
 */
@FunctionalInterface
public interface Action {
    /**
     * An empty action.
     */
    Action EMPTY = () -> {};

    /**
     * Performs the action.
     */
    void act();
}
