package net.minestom.demo.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;

public class LoadedChunksCommand extends Command {
    public LoadedChunksCommand() {
        super("loadedchunks");
        setDefaultExecutor((sender, context) -> {
            for (var instance : MinecraftServer.getInstanceManager().getInstances()) {
                sender.sendMessage(instance.getUuid() + ": " + instance.getChunks().size());
            }
        });
    }
}
