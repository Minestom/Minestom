package net.minestom.server.command;

import net.minestom.server.entity.Player;
import net.minestom.server.permission.Permission;

import java.util.Collection;

/**
 * Represent something which can send commands to the server
 * <p>
 * Main implementations are {@link Player} and {@link ConsoleSender}
 */
public interface CommandSender {

    /**
     * Send a raw string message
     *
     * @param message the message to send
     */
    void sendMessage(String message);

    /**
     * Send multiple raw string messages
     *
     * @param messages the messages to send
     */
    default void sendMessage(String[] messages) {
        for (String message : messages) {
            sendMessage(message);
        }
    }

    /**
     * Return all permissions associated to this command sender.
     * The returned collection should be modified only by subclasses
     * @return
     */
    Collection<Permission> getAllPermissions();

    /**
     * Adds a permission to this commandSender
     * @param permission
     */
    default void addPermission(Permission permission) {
        getAllPermissions().add(permission);
    }

    /**
     * Removes a permission from this commandSender
     * @param permission
     */
    default void removePermission(Permission permission) {
        getAllPermissions().remove(permission);
    }

    /**
     * Checks if the given permission is possessed by this command sender.
     * Simple shortcut to <pre>getAllPermissions().contains(permission) &amp;&amp; permission.isValidFor(this)</pre> for readability.
     * @param p permission to check against
     * @return
     */
    default boolean hasPermission(Permission p) {
        return getAllPermissions().contains(p) && p.isValidFor(this);
    }

    /**
     * Checks if the given permission is possessed by this command sender.
     * Will call {@link Permission#isValidFor(CommandSender)} on all permissions that are an instance of permissionClass.
     * If no matching permission is found, this result returns false.
     *
     * @param permissionClass
     * @see #getAllPermissions()
     * @return
     */
    default boolean hasPermission(Class<? extends Permission> permissionClass) {
        boolean result = true;
        boolean foundPerm = false;
        for(Permission p : getAllPermissions()) {
            if(permissionClass.isInstance(p)) {
                foundPerm = true;
                result &= p.isValidFor(this);
            }
        }
        if(!foundPerm)
            return false;
        return result;
    }

    /**
     * Get if the sender is a player
     *
     * @return true if 'this' is a player, false otherwise
     */
    default boolean isPlayer() {
        return this instanceof Player;
    }

    /**
     * Get if the sender is the console
     *
     * @return true if 'this' is the console, false otherwise
     */
    default boolean isConsole() {
        return this instanceof ConsoleSender;
    }

    /**
     * Casts this object to a Player
     * No checks are performed, {@link ClassCastException} can very much happen
     * @see #isPlayer()
     */
    default Player asPlayer() {
        return (Player)this;
    }

    /**
     * Casts this object to a ConsoleSender
     * No checks are performed, {@link ClassCastException} can very much happen
     * @see #isConsole()
     */
    default ConsoleSender asConsole() {
        return (ConsoleSender)this;
    }
}
