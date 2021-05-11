package net.minestom.demo.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.extensions.ExtensionManager;

import java.io.IOException;
import java.nio.file.Path;

public class LoadExtensionCommand extends Command {

    private final ArgumentString extensionName;

    public LoadExtensionCommand() {
        super("load");

        setDefaultExecutor(this::usage);

        extensionName = ArgumentType.String("extensionName");

        setArgumentCallback(this::extensionCallback, extensionName);
        addSyntax(this::execute, extensionName);
    }

    private void usage(CommandSender sender, CommandContext context) {
        sender.sendMessage(Component.text("Usage: /load <extension file name>"));
    }

    private void execute(CommandSender sender, CommandContext context) {
        final String name = context.get(extensionName);
        sender.sendMessage(Component.text("extensionFile = " + name + "...."));

        ExtensionManager extensionManager = MinecraftServer.getExtensionManager();
        Path extensionFolder = extensionManager.getExtensionFolder().toPath().toAbsolutePath();
        Path extensionJar = extensionFolder.resolve(name);
        try {
            if (!extensionJar.toFile().getCanonicalPath().startsWith(extensionFolder.toFile().getCanonicalPath())) {
                sender.sendMessage(Component.text("File name '" + name + "' does not represent a file inside the extensions folder. Will not load"));
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            sender.sendMessage(Component.text("Failed to load extension: " + e.getMessage()));
            return;
        }

        try {
            boolean managed = extensionManager.loadDynamicExtension(extensionJar.toFile());
            if (managed) {
                sender.sendMessage(Component.text("Extension loaded!"));
            } else {
                sender.sendMessage(Component.text("Failed to load extension, check your logs."));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(Component.text("Failed to load extension: " + e.getMessage()));
        }
    }

    private void extensionCallback(CommandSender sender, ArgumentSyntaxException exception) {
        sender.sendMessage(Component.text("'" + exception.getInput() + "' is not a valid extension name!"));
    }
}
