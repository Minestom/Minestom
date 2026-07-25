package net.minestom.demo.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * A simple shutdown command.
 */
public class SaveCommand extends Command {

    public SaveCommand() {
        super("save");
        addSyntax(SaveCommand::execute);
    }

    private static void execute(CommandSender commandSender, CommandContext commandContext) {
        for(var instance : MinecraftServer.getInstanceManager().getInstances()) {
            CompletableFuture<Void> instanceSave = instance.saveInstance().thenCompose(_ -> instance.saveChunksToStorage());
            try {
                instanceSave.get();
            } catch (InterruptedException | ExecutionException e) {
                MinecraftServer.getExceptionManager().handleException(e);
            }
        }
        commandSender.sendMessage("Saving done!");
    }
}
