package net.minestom.demo.core;

import net.minestom.server.ServerProcess;

/**
 * A self-contained slice of demo functionality.
 * <p>
 * Implementations register commands, listeners, handlers, recipes,
 * etc. against the given {@link ServerProcess}. No static
 * {@code MinecraftServer.getX()} access — everything goes through
 * the process so features stay isolated and reorderable.
 */
@FunctionalInterface
public interface Feature {

    /**
     * Register this feature against the given server process.
     * Called once, before the server binds to a port.
     */
    void register(ServerProcess process);
}
