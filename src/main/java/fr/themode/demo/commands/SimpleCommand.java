package fr.themode.demo.commands;

import fr.themode.minestom.command.CommandProcessor;
import fr.themode.minestom.entity.Player;

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
