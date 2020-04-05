package fr.themode.minestom.command;

import fr.themode.minestom.entity.Player;

public interface CommandProcessor {

    String getCommandName();

    boolean process(Player player, String command, String[] args);
}
