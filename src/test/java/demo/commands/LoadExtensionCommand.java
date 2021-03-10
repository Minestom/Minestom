package demo.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.extensions.ExtensionManager;

import java.io.IOException;
import java.nio.file.Path;

public class LoadExtensionCommand extends Command {
    public LoadExtensionCommand() {
        super("load");

        setDefaultExecutor(this::usage);

        var extension = ArgumentType.DynamicStringArray("extensionName");

        setArgumentCallback(this::extensionCallback, extension);

        addSyntax(this::execute, extension);
    }

    private void usage(CommandSender sender, CommandContext context) {
        sender.sendMessage("Usage: /load <extension file name>");
    }

    private void execute(CommandSender sender, CommandContext context) {
        String name = join(context.getStringArray("extensionName"));
        sender.sendMessage("extensionFile = " + name + "....");

        ExtensionManager extensionManager = MinecraftServer.getExtensionManager();
        Path extensionFolder = extensionManager.getExtensionFolder().toPath().toAbsolutePath();
        Path extensionJar = extensionFolder.resolve(name);
        try {
            if (!extensionJar.toFile().getCanonicalPath().startsWith(extensionFolder.toFile().getCanonicalPath())) {
                sender.sendMessage("File name '" + name + "' does not represent a file inside the extensions folder. Will not load");
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            sender.sendMessage("Failed to load extension: " + e.getMessage());
            return;
        }

        try {
            boolean managed = extensionManager.loadDynamicExtension(extensionJar.toFile());
            if (managed) {
                sender.sendMessage("Extension loaded!");
            } else {
                sender.sendMessage("Failed to load extension, check your logs.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage("Failed to load extension: " + e.getMessage());
        }
    }

    private void extensionCallback(CommandSender sender, ArgumentSyntaxException exception) {
        sender.sendMessage("'" + exception.getInput() + "' is not a valid extension name!");
    }

    private String join(String[] extensionNameParts) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < extensionNameParts.length; i++) {
            String s = extensionNameParts[i];
            if (i != 0) {
                b.append(" ");
            }
            b.append(s);
        }
        return b.toString();
    }
}
