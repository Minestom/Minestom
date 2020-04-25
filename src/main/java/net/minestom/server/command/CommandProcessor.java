package net.minestom.server.command;

import net.minestom.server.entity.Player;

public interface CommandProcessor {

    String getCommandName();

    String[] getAliases();

    boolean process(Player player, String command, String[] args);
}
