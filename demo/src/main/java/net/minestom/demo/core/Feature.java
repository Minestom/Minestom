package net.minestom.demo.core;

import net.minestom.server.ServerProcess;

/** A self-contained slice of demo functionality, attached to a {@link ServerProcess}. */
@FunctionalInterface
public interface Feature {
    void register(ServerProcess process);
}
