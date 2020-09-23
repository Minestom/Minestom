package net.minestom.server.permission;

import net.minestom.server.command.CommandSender;
import net.minestom.server.data.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Representation of a permission granted to a CommandSender
 */
@FunctionalInterface
public interface Permission {

    /**
     * Does the given commandSender have the permission represented by this object?
     *
     * @param commandSender the command sender
     * @return true if the commandSender possesses this permission
     */
    boolean isValidFor(CommandSender commandSender);

    /**
     * Writes any required data for this permission inside the given destination
     *
     * @param destination Data to write to
     */
    default void write(@NotNull Data destination) {
    }

    /**
     * Reads any required data for this permission from the given destination
     *
     * @param source Data to read from
     * @return this for chaining
     */
    default Permission read(@Nullable Data source) {
        return this;
    }
}
