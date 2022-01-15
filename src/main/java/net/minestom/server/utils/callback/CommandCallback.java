package net.minestom.server.utils.callback;

import net.minestom.server.command.CommandOrigin;
import net.minestom.server.command.StringReader;
import org.jetbrains.annotations.NotNull;

/**
 * Functional interface used by the {@link net.minestom.server.command.CommandManager}
 * to execute a callback if an unknown command is run.
 * You can set it with {@link net.minestom.server.command.CommandManager#setUnknownCommandCallback(CommandCallback)}.
 */
@FunctionalInterface
public interface CommandCallback {

    /**
     * Executed if an unknown command is run.
     *
     * @param origin the origin of the command
     * @param command the StringReader containing the command, where the unread portion of the reader should be treated
     *                as the command
     */
    void apply(@NotNull CommandOrigin origin, @NotNull StringReader command);

}
