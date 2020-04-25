package fr.themode.demo.commands;

import net.minestom.server.command.CommandProcessor;
import net.minestom.server.entity.Player;

public class SimpleCommand implements CommandProcessor {
    @Override
    public String getCommandName() {
        return "test";
    }

    @Override
    public boolean process(Player player, String command, String[] args) {
        player.sendMessage("You tried the sample command!");

        return true;
    }
}
