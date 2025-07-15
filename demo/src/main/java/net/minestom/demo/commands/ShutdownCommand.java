package net.minestom.demo.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;

/**
 * A simple shutdown command.
 */
public class ShutdownCommand extends Command {

    public ShutdownCommand() {
        super("shutdown");
        addSyntax(this::execute);
    }

    private void execute(CommandSender commandSender, CommandContext commandContext) {
        MinecraftServer.stopCleanly();
    }
}
