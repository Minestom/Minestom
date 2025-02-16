package net.minestom.demo.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.LightingChunk;

import java.util.concurrent.TimeUnit;

public class RelightCommand extends Command {
    public RelightCommand() {
        super("relight");
        setDefaultExecutor((source, args) -> {
            if (source instanceof Player player) {
                long start = System.nanoTime();
                source.sendMessage("Relighting...");
                var relit = LightingChunk.relight(player.getInstance(), player.getInstance().getChunks());
                source.sendMessage("Relighted " + player.getInstance().getChunks().size() + " chunks in " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + "ms");
                relit.forEach(chunk -> chunk.sendChunk(player));
                source.sendMessage("Chunks Received");
            }
        });
    }
}