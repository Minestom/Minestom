package fr.themode.demo.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandProcessor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.world.Dimension;

import java.util.Optional;

public class DimensionCommand implements CommandProcessor {
    @Override
    public String getCommandName() {
        return "dimensiontest";
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public boolean process(CommandSender sender, String command, String[] args) {

        if (!sender.isPlayer())
            return false;
        Player player = (Player) sender;

        Instance instance = player.getInstance();

        Dimension targetDimension = Dimension.NETHER;
        if (instance.getDimension() == Dimension.NETHER) {
            targetDimension = Dimension.OVERWORLD;
        }

        Dimension finalTargetDimension = targetDimension;
        Optional<Instance> targetInstance = MinecraftServer.getInstanceManager().getInstances().stream().filter(in -> in.getDimension() == finalTargetDimension).findFirst();
        if (targetInstance.isPresent()) {
            player.sendMessage("You were in " + instance.getDimension());
            player.setInstance(targetInstance.get());
            player.sendMessage("You are now in " + targetDimension);
        } else {
            player.sendMessage("Could not find instance with dimension " + targetDimension);
        }

        return true;
    }

    @Override
    public boolean hasAccess(Player player) {
        return true;
    }
}
