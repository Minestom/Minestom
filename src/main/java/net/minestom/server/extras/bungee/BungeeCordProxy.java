package net.minestom.server.extras.bungee;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * BungeeCord forwarding support. Enabling BungeeGuard support with {@link #setBungeeGuardTokens(Set)} helps to secure the server,
 * but managing your firewall is still recommended.
 * <p>
 * Please consider using {@link net.minestom.server.extras.velocity.VelocityProxy} instead.
 */
public final class BungeeCordProxy {

    private static Set<String> bungeeGuardTokens = null;

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

    /**
     * Sets the tokens used by BungeeGuard authentication.
     * Setting the tokens to a not-null value enables BungeeGuard authentication,
     * and setting it to a null value disables BungeeGuard authentication.
     *
     * @param tokens The new BungeeGuard authentication tokens
     */
    public static void setBungeeGuardTokens(@Nullable Set<String> tokens) {
        bungeeGuardTokens = tokens;
    }

    /**
     * Checks whether BungeeGuard authentication is enabled.
     *
     * @return Whether BungeeGuard authentication is enabled
     */
    public static boolean isBungeeGuardEnabled() {
        return bungeeGuardTokens != null;
    }

    /**
     * Checks whether a token is one of the valid BungeeGuard tokens
     *
     * @param token The token to test
     * @return Whether the token is a valid BungeeGuard token
     */
    public static boolean isValidBungeeGuardToken(@NotNull String token) {
        return isBungeeGuardEnabled() && bungeeGuardTokens.contains(token);
    }

}
