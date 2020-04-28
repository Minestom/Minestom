package fr.themode.demo.commands;

import net.minestom.server.command.CommandProcessor;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.MathUtils;

public class SimpleCommand implements CommandProcessor {
    @Override
    public String getCommandName() {
        return "test";
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public boolean process(Player player, String command, String[] args) {
        player.sendMessage("Everyone come at you!");

        Instance instance = player.getInstance();

        for (EntityCreature creature : instance.getCreatures()) {
            creature.setPathTo(player.getPosition());
        }

        player.sendMessage("Direction: "+ MathUtils.getHorizontalDirection(player.getPosition().getYaw()));


        return true;
    }

    @Override
    public boolean hasAccess(Player player) {
        return true;
    }
}
