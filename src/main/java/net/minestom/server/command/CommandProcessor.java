package net.minestom.server.command;

import net.minestom.server.entity.Player;

/**
 * Represent a simple command which give you the whole string representation
 */
public interface CommandProcessor {

    /**
     * Get the main command's name
     *
     * @return the main command's name
     */
    String getCommandName();

    /**
     * Get the command's aliases
     * <p>
     * Can be null or empty
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
     * Called to know if a player has access to the command
     * <p>
     * Right now it is only used to know if the player should see the command in auto-completion
     * Conditions still need to be checked in {@link #process(CommandSender, String, String[])}
     *
     * @param player the player to check the access
     * @return true if the player has access to the command, false otherwise
     */
    boolean hasAccess(Player player);
}
