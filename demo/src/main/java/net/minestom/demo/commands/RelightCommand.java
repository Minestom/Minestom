package net.minestom.demo.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Chunk;
import net.minestom.server.utils.chunk.ChunkUtils;

public class RelightCommand extends Command {
    public RelightCommand() {
        super("relight");
        setDefaultExecutor(this::usage);

        addSyntax((sender, context) -> {
            assert ((Player) sender).getInstance() != null;
            var chunks = ((Player) sender).getInstance().getChunks();

            sender.sendMessage(Component.text("Relighting " + chunks.size() + " chunks..."));

            int sections = 0;
            for (Chunk chunk : chunks) {
                sections += chunk.getMaxSection() - chunk.getMinSection() + 1;
            }

            long startTime = System.nanoTime();
            ChunkUtils.relight(((Player) sender).getInstance(), chunks);
            long length = (System.nanoTime() - startTime) / 1000000;
            sender.sendMessage("Relit " + chunks.size() + " chunks in " + length + "ms (" + sections + " sections)");

            // for (int x = 0; x < 15; ++x) {
            //     long startTime = System.nanoTime();
            //     ChunkUtils.relight(((Player) sender).getInstance(), chunks);
            //     long length = (System.nanoTime() - startTime) / 1000000;
            //     sender.sendMessage("Relit " + chunks.size() + " chunks in " + length + "ms (" + sections + " sections)");
            // }
            // System.out.println("PERFORMANCE DONE");

            chunks.forEach(Chunk::sendChunk);
        });
    }

    private void usage(CommandSender sender, CommandContext context) {
        sender.sendMessage(Component.text("Incorrect usage"));
    }

}
