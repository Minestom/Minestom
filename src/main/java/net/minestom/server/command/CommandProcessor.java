package net.minestom.server.command;

import net.minestom.server.entity.Player;

public interface CommandProcessor {

    String getCommandName();

    String[] getAliases();

    boolean process(CommandSender sender, String command, String[] args);

    boolean hasAccess(Player player);
}
