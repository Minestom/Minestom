package net.minestom.server.permission;

import net.minestom.server.command.CommandSender;

/**
 * Representation of a permission granted to a CommandSender
 */
public interface Permission {

    /**
     * Does the given commandSender have the permission represented by this object?
     * @param commandSender
     * @return true if the commandSender possesses this permission
     */
    boolean isValidFor(CommandSender commandSender);

    // TODO: Serialization?
}
