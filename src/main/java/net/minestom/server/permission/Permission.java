package net.minestom.server.permission;

import net.minestom.server.command.CommandSender;
import net.minestom.server.data.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Representation of a permission granted to a {@link CommandSender}
 */
@FunctionalInterface
public interface Permission {

    /**
     * Does the given {@link CommandSender} have the permission represented by this object?
     * <p>
     * Called with {@link CommandSender#hasPermission(Permission)}, the {@link CommandSender} requires to both
     * have this permission and validate the condition in this method.
     *
     * @param commandSender the command sender
     * @return true if the commandSender possesses this permission
     */
    boolean isValidFor(CommandSender commandSender);

    /**
     * Writes any required data for this permission inside the given destination
     *
     * @param destination {@link Data} to write to
     */
    default void write(@NotNull Data destination) {
    }

    /**
     * Reads any required data for this permission from the given destination
     *
     * @param source {@link Data} to read from
     * @return this for chaining
     */
    default Permission read(@Nullable Data source) {
        return this;
    }
}
