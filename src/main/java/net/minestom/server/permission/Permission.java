package net.minestom.server.permission;

import net.minestom.server.command.CommandSender;
import net.minestom.server.data.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Representation of a permission granted to a {@link CommandSender}.
 *
 * @param <T> the type of data that this permission can handle in {@link #isValidFor(CommandSender, Object)}.
 *            Used if you want to allow passing additional data to check if the permission is valid in a certain situation,
 *            you can default it to {@link Object} if you do not need it.
 */
@FunctionalInterface
public interface Permission<T> {

    /**
     * Does the given {@link CommandSender} have the permission represented by this object?
     * <p>
     * Called with {@link CommandSender#hasPermission(Permission)}, the {@link CommandSender} requires to both
     * have this permission and validate the condition in this method.
     *
     * @param commandSender the command sender
     * @param data          the optional data (eg the number of home possible, placing a block at X position)
     * @return true if the commandSender possesses this permission
     */
    boolean isValidFor(@NotNull CommandSender commandSender, @Nullable T data);

    /**
     * Writes any required data for this permission inside the given destination.
     *
     * @param destination the {@link Data} to write to
     */
    default void write(@NotNull Data destination) {
    }

    /**
     * Reads any required data for this permission from the given destination.
     *
     * @param source the {@link Data} to read from
     * @return this for chaining
     */
    default Permission read(@Nullable Data source) {
        return this;
    }
}
