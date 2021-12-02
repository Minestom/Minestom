package net.minestom.server.command.builder.condition;

import net.minestom.server.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Used to know if the {@link CommandSender} is allowed to run the command or a specific syntax.
 */
@FunctionalInterface
public interface CommandCondition {

    /**
     * Called when the sender's identity, their permissions, or the provided command needs to be checked to see if it
     * will run.<br>
     * Whenever command nodes are being generated for a source, this method will be called with {@code commandString} as
     * null to find out if the command or syntax should be sent to the player for them to tab complete. Command nodes
     * are usually generated when commands are refreshed for a sender or a player joins the game.<br>
     * Other than those cases, {@code commandString} will never be null, and will instead be the command that was run.
     * <br>
     * When using this, you should warn the sender (e.g. by sending a message) if the condition is unsuccessful.
     *
     * @param sender        the sender of the command
     * @param commandString the raw command string (null if this is an access request)
     * @return true if the sender should be able to use the command, otherwise false
     */
    boolean canUse(@NotNull CommandSender sender, @Nullable String commandString);
}
