package net.minestom.server.utils;

import net.minestom.server.MinecraftServer;
import net.minestom.server.monitoring.BenchmarkManager;

/**
 * Utility class for retrieving server statistics.
 * If you want access to specific information like CPU usage, use the {@link BenchmarkManager} class from {@link MinecraftServer#getBenchmarkManager()}.
 */
public final class Stats {
    private Stats() {}

    /**
     * Gets the heap memory used by the server in bytes.
     *
     * @return the memory used by the server
     */
    public static long getUsedMemory() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }
}
