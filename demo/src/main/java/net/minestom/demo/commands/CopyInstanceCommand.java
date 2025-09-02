package net.minestom.demo.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceContainer;

import java.util.List;

public class CopyInstanceCommand extends Command {
    public CopyInstanceCommand() {
        super("copyinstance");
        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            var instance = player.getInstance();
            if (instance == null) return;
            if (!(instance instanceof InstanceContainer container)) return;
            var copy = container.copy();
            var chunks = List.copyOf(copy.getChunks());
            MinecraftServer.getInstanceManager().registerInstance(copy);
            player.setInstance(copy).join();
        });
    }
}
