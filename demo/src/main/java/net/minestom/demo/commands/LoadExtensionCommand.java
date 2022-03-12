package net.minestom.demo.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandOrigin;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.extensions.ExtensionManager;
import org.jetbrains.annotations.NotNull;

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

    private void usage(@NotNull CommandOrigin origin, @NotNull CommandContext context) {
        origin.sender().sendMessage(Component.text("Usage: /load <extension file name>"));
    }

    private void execute(@NotNull CommandOrigin origin, @NotNull CommandContext context) {
        final String name = context.get(extensionName);
        origin.sender().sendMessage(Component.text("extensionFile = " + name + "...."));

        ExtensionManager extensionManager = MinecraftServer.getExtensionManager();
        Path extensionFolder = extensionManager.getExtensionFolder().toPath().toAbsolutePath();
        Path extensionJar = extensionFolder.resolve(name);
        try {
            if (!extensionJar.toFile().getCanonicalPath().startsWith(extensionFolder.toFile().getCanonicalPath())) {
                origin.sender().sendMessage(Component.text("File name '" + name + "' does not represent a file inside the extensions folder. Will not load"));
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            origin.sender().sendMessage(Component.text("Failed to load extension: " + e.getMessage()));
            return;
        }

        try {
            boolean managed = extensionManager.loadDynamicExtension(extensionJar.toFile());
            if (managed) {
                origin.sender().sendMessage(Component.text("Extension loaded!"));
            } else {
                origin.sender().sendMessage(Component.text("Failed to load extension, check your logs."));
            }
        } catch (Exception e) {
            e.printStackTrace();
            origin.sender().sendMessage(Component.text("Failed to load extension: " + e.getMessage()));
        }
    }

    private void extensionCallback(@NotNull CommandOrigin origin, @NotNull CommandException exception) {
        origin.sender().sendMessage(Component.text("Invalid extension", NamedTextColor.RED));
        origin.sender().sendMessage(exception.generateContextMessage());
    }
}
