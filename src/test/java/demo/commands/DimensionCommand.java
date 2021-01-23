package demo.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandProcessor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class DimensionCommand implements CommandProcessor {
    @NotNull
    @Override
    public String getCommandName() {
        return "dimensiontest";
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public boolean process(@NotNull CommandSender sender, @NotNull String command, @NotNull String[] args) {

        if (!sender.isPlayer())
            return false;
        Player player = (Player) sender;

        Instance instance = player.getInstance();

        DimensionType targetDimensionType = DimensionType.OVERWORLD;
        //if (instance.getDimensionType() == targetDimensionType) {
        //    targetDimensionType = DimensionType.OVERWORLD;
        //}

        Optional<Instance> targetInstance = MinecraftServer.getInstanceManager().getInstances().stream().filter(in -> in.getDimensionType() == targetDimensionType).findFirst();
        if (targetInstance.isPresent()) {
            player.sendMessage("You were in " + instance.getDimensionType());
            player.setInstance(targetInstance.get());
            player.sendMessage("You are now in " + targetDimensionType);
        } else {
            player.sendMessage("Could not find instance with dimension " + targetDimensionType);
        }

        return true;
    }

    @Override
    public boolean hasAccess(@NotNull Player player) {
        return true;
    }
}
