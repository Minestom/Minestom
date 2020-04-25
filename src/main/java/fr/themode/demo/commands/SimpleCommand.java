package fr.themode.demo.commands;

import net.minestom.server.command.CommandProcessor;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

public class SimpleCommand implements CommandProcessor {
    @Override
    public String getCommandName() {
        return "test";
    }

    @Override
    public boolean process(Player player, String command, String[] args) {
        player.sendMessage("You tried the sample command!");

        Instance instance = player.getInstance();

        for (EntityCreature creature : instance.getCreatures()) {
            creature.setPathTo(player.getPosition());
        }

        return true;
    }
}
