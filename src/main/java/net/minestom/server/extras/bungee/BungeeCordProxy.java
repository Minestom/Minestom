package net.minestom.server.extras.bungee;

/**
 * BungeeCord forwarding support. This does not count as a security feature, and you will still be required to manage your firewall.
 * <p>
 * Please consider using {@link net.minestom.server.extras.velocity.VelocityProxy} instead.
 */
public final class BungeeCordProxy {

    private static volatile boolean enabled;

    /**
     * Enables bungee IP forwarding.
     */
    public static void enable() {
        BungeeCordProxy.enabled = true;
    }

    /**
     * Gets if bungee IP forwarding is enabled.
     *
     * @return true if forwarding is enabled
     */
    public static boolean isEnabled() {
        return enabled;
    }
}
