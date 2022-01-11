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
     * Whenever command nodes are being generated for a source, this method will be called with {@code command} as null
     * and {@code startingPosition} as -1 to find out if the command or syntax should be sent to the player for them to
     * tab complete. Command nodes are usually generated when commands are refreshed for a sender or a player joins the
     * game, although theoretically they could be refreshed at any time.<br>
     * Other than those cases, {@code command} should never be null and will instead be the command that was run and
     * {@code startingPosition} will be valid.<br>
     * When using this, you should warn the sender (e.g. by sending a message) if the condition is unsuccessful.
     * @param sender        the sender of the command
     * @param command the raw command string (null if this is an access request)
     * @param startingPosition the starting position in the provided command. For example, if someone runs "/hello", the
     *                         starting position will be 1 because that is the length of the slash that is at the start.
     * @return true if the sender should be able to use the command, otherwise false
     */
    boolean canUse(@NotNull CommandSender sender, @Nullable String command, int startingPosition);

}
