package net.minestom.server.command;

import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Represents something which can send commands to the server.
 * <p>
 * Main implementations are {@link Player} and {@link ConsoleSender}.
 */
public interface CommandSender {

    /**
     * Sends a raw string message.
     *
     * @param message the message to send
     */
    void sendMessage(@NotNull String message);

    /**
     * Sends multiple raw string messages.
     *
     * @param messages the messages to send
     */
    default void sendMessage(@NotNull String[] messages) {
        for (String message : messages) {
            sendMessage(message);
        }
    }

    /**
     * Returns all permissions associated to this command sender.
     * The returned collection should be modified only by subclasses.
     *
     * @return the permissions of this command sender.
     */
    @NotNull
    Collection<Permission> getAllPermissions();

    /**
     * Adds a {@link Permission} to this commandSender
     *
     * @param permission the permission to add
     */
    default void addPermission(@NotNull Permission permission) {
        getAllPermissions().add(permission);
    }

    /**
     * Removes a {@link Permission} from this commandSender
     *
     * @param permission the permission to remove
     */
    default void removePermission(@NotNull Permission permission) {
        getAllPermissions().remove(permission);
    }

    /**
     * Checks if the given {@link Permission} is possessed by this command sender.
     * Simple shortcut to <pre>getAllPermissions().contains(permission) &amp;&amp; permission.isValidFor(this)</pre> for readability.
     *
     * @param p permission to check against
     * @return true if the sender has the permission and validate {@link Permission#isValidFor(CommandSender)}
     */
    default boolean hasPermission(@NotNull Permission p) {
        return getAllPermissions().contains(p) && p.isValidFor(this);
    }

    /**
     * Checks if the given {@link Permission} is possessed by this command sender.
     * Will call {@link Permission#isValidFor(CommandSender)} on all permissions that are an instance of {@code permissionClass}.
     * If no matching permission is found, this result returns false.
     *
     * @param permissionClass the permission class to check
     * @return true if the sender has the permission and validate {@link Permission#isValidFor(CommandSender)}
     * @see #getAllPermissions()
     */
    default boolean hasPermission(@NotNull Class<? extends Permission> permissionClass) {
        boolean result = true;
        boolean foundPerm = false;
        for (Permission p : getAllPermissions()) {
            if (permissionClass.isInstance(p)) {
                foundPerm = true;
                result &= p.isValidFor(this);
            }
        }
        if (!foundPerm)
            return false;
        return result;
    }

    /**
     * Gets if the sender is a {@link Player}.
     *
     * @return true if 'this' is a player, false otherwise
     */
    default boolean isPlayer() {
        return this instanceof Player;
    }

    /**
     * Gets if the sender is a {@link ConsoleSender}.
     *
     * @return true if 'this' is the console, false otherwise
     */
    default boolean isConsole() {
        return this instanceof ConsoleSender;
    }

    /**
     * Casts this object to a {@link Player}.
     * No checks are performed, {@link ClassCastException} can very much happen.
     *
     * @see #isPlayer()
     */
    default Player asPlayer() {
        return (Player) this;
    }

    /**
     * Casts this object to a {@link ConsoleSender}.
     * No checks are performed, {@link ClassCastException} can very much happen.
     *
     * @see #isConsole()
     */
    default ConsoleSender asConsole() {
        return (ConsoleSender) this;
    }
}
