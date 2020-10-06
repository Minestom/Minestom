package net.minestom.server.command;

import net.minestom.server.entity.Player;

/**
 * Represents a simple command which give you the whole string representation
 */
public interface CommandProcessor {

    /**
     * Get the main command's name.
     *
     * @return the main command's name
     */
    String getCommandName();

    /**
     * Get the command's aliases.
     * <p>
     * Can be null or empty.
     *
     * @return the command aliases
     */
    String[] getAliases();

    /**
     * Called when the command is executed by a {@link CommandSender}
     *
     * @param sender  the sender which executed the command
     * @param command the command name used
     * @param args    an array containing all the args (split by space char)
     * @return true when the command is successful, false otherwise
     */
    boolean process(CommandSender sender, String command, String[] args);

    /**
     * Called to know if a player has access to the command.
     * <p>
     * Right now it is only used to know if the player should see the command in auto-completion
     * Conditions still need to be checked in {@link #process(CommandSender, String, String[])}
     *
     * @param player the player to check the access
     * @return true if the player has access to the command, false otherwise
     */
    boolean hasAccess(Player player);

    /**
     * Needed to enable {@link #onWrite(String)} callback
     * <p>
     * Be aware that enabling it can cost some performance because of how often it will be called.
     *
     * @return true to enable writing tracking (and server auto completion)
     * @see #onWrite(String)
     */
    default boolean enableWritingTracking() {
        return false;
    }

    /**
     * Allow for tab auto completion, this is called everytime the player press a key in the chat.
     * <p>
     * WARNING: {@link #enableWritingTracking()} needs to return true, you need to override it by default.
     *
     * @param text the whole player text
     * @return the array containing all the suggestion for the current arg (split " ")
     * @see #enableWritingTracking()
     */
    default String[] onWrite(String text) {
        return null;
    }
}
