package net.minestom.demo.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

import java.util.List;

public class InstanceCommand extends Command {

    public InstanceCommand() {
        super("instance");

        ArgumentInteger argumentInteger = ArgumentType.Integer("index");

        addSyntax((sender, context) -> {
            Player player = (Player) sender;
            List<Instance> instances = MinecraftServer.getInstanceManager().getInstances().stream().toList();
            int index = context.get(argumentInteger);
            if (index < 0 || index >= instances.size()) {
                player.sendMessage(Component.text("Instance out of bounds of 0-" + (instances.size() - 1)));
                return;
            }
            Instance instance = instances.get(index);
            if (player.getInstance().equals(instance)) {
                player.sendMessage(Component.text("Already in that instance!", NamedTextColor.RED));
                return;
            }

            player.setInstance(instance, player.getPosition()).thenRun(()
                    -> player.scheduler().scheduleNextTick(() -> player.sendMessage("Your new ticking thread: " + player.acquirable().assignedThread().getName())));
        }, argumentInteger);
    }

}
