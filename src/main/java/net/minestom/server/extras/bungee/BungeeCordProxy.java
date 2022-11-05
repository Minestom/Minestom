package net.minestom.server.extras.bungee;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

    /**
     * Indicates that a BungeeGuard authentication was invalid due missing, multiple, or invalid tokens.
     */
    public static final Component INVALID_TOKEN = Component.text("Invalid connection, please connect through the proxy", NamedTextColor.RED);

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

    /**
     * Gets the maximum length of the handshake packet.
     *
     * @return The maximum length of the handshake packet
     */
    public static int getMaxHandshakeLength() {
        // BungeeGuard limits handshake length to 2500 characters, while vanilla limits it to 255
        return isEnabled() ? (isBungeeGuardEnabled() ? 2500 : Short.MAX_VALUE) : 255;
    }

}
