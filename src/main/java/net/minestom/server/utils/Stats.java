package net.minestom.server.utils;

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
