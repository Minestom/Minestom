package net.minestom.server.command;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a simple command which give you the whole string representation.
 * <p>
 * {@link #process(CommandSender, String, String[])} is called no matter what if a {@link CommandSender} sends a command which
 * start by {@link #getCommandName()} or any of the aliases in {@link #getAliases()}.
 * <p>
 * Tab-completion can be activated by overriding {@link #enableWritingTracking()} and return true, you should then listen to
 * {@link #onWrite(CommandSender, String)} and return the possible completions to suggest.
 * <p>
 * Please be sure to check {@link net.minestom.server.command.builder.Command} as it is likely to be better for your use case.
 *
 * @deprecated use {@link net.minestom.server.command.builder.Command} or
 * {@link net.minestom.server.command.builder.SimpleCommand} instead
 */
@Deprecated
public interface CommandProcessor {

    /**
     * Gets the main command's name.
     *
     * @return the main command's name
     */
    @NotNull
    String getCommandName();

    /**
     * Gets the command's aliases.
     * <p>
     * Can be null or empty.
     *
     * @return the command aliases
     */
    @Nullable
    String[] getAliases();

    /**
     * Called when the command is executed by a {@link CommandSender}.
     *
     * @param sender  the sender which executed the command
     * @param command the command name used
     * @param args    an array containing all the args (split by space char)
     * @return true when the command is successful, false otherwise
     */
    boolean process(@NotNull CommandSender sender, @NotNull String command, @NotNull String[] args);

    /**
     * Called to know if a player has access to the command.
     * <p>
     * Right now it is only used to know if the player should see the command in auto-completion.
     * Conditions still need to be checked in {@link #process(CommandSender, String, String[])}.
     *
     * @param player the player to check the access
     * @return true if the player has access to the command, false otherwise
     */
    boolean hasAccess(@NotNull Player player);

    /**
     * Needed to enable {@link #onWrite(CommandSender, String)} callback.
     * <p>
     * Be aware that enabling it can cost some performance because of how often it will be called.
     *
     * @return true to enable writing tracking (and server auto completion)
     * @see #onWrite(CommandSender, String)
     */
    default boolean enableWritingTracking() {
        return false;
    }

    /**
     * Allows for tab auto completion, this is called everytime the player press a key in the chat.
     * <p>
     * WARNING: {@link #enableWritingTracking()} needs to return true, you need to override it by default.
     *
     * @param sender the command sender
     * @param text   the whole player text
     * @return the array containing all the suggestions for the current arg (split SPACE), can be null
     * @see #enableWritingTracking()
     * @deprecated not called anymore
     */
    @Deprecated
    @Nullable
    default String[] onWrite(@NotNull CommandSender sender, String text) {
        return null;
    }
}
