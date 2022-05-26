package net.minestom.demo.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.chunk.ChunkUtils;

public class RelightCommand extends Command {
    public RelightCommand() {
        super("relight");
        setDefaultExecutor(this::usage);

        addSyntax((sender, context) -> {
            assert ((Player) sender).getInstance() != null;
            ChunkUtils.relight(((Player)sender).getInstance(), ((Player) sender).getInstance().getChunks());
        });
    }

    private void usage(CommandSender sender, CommandContext context) {
        sender.sendMessage(Component.text("Incorrect usage"));
    }

}
