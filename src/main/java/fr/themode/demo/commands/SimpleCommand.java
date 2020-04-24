package fr.themode.demo.commands;

import fr.themode.demo.entity.ChickenCreature;
import net.minestom.server.command.CommandProcessor;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.Player;

public class SimpleCommand implements CommandProcessor {
    @Override
    public String getCommandName() {
        return "test";
    }

    @Override
    public boolean process(Player player, String command, String[] args) {
        player.sendMessage("You tried the sample command!");


        for (EntityCreature entity : player.getInstance().getCreatures()) {
            if (entity instanceof ChickenCreature) {
                ChickenCreature chickenCreature = (ChickenCreature) entity;
                chickenCreature.moveTo(player.getPosition().clone());
                player.sendMessage("CHICKEN GO");
            }
        }

        return true;
    }
}
