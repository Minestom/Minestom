package net.minestom.demo.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

import java.util.concurrent.ThreadLocalRandom;

public class DimensionCommand extends Command {

    public DimensionCommand() {
        super("dimensiontest");
        setCondition(Conditions::playerOnly);

        addSyntax((sender, context) -> {
            final Player player = (Player) sender;
            final Instance instance = player.getInstance();
            final var instances = MinecraftServer.getInstanceManager().getInstances().stream().filter(instance1 -> !instance1.equals(instance)).toList();
            if (instances.isEmpty()) {
                player.sendMessage("No instance available");
                return;
            }
            final var newInstance = instances.get(ThreadLocalRandom.current().nextInt(instances.size()));
            player.setInstance(newInstance).thenRun(() -> player.sendMessage("Teleported"));
        });
    }
}
