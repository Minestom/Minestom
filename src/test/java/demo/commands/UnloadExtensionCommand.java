package demo.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.extensions.Extension;
import net.minestom.server.extensions.ExtensionManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class UnloadExtensionCommand extends Command {

    private final ArgumentString extensionName;

    public UnloadExtensionCommand() {
        super("unload");

        setDefaultExecutor(this::usage);

        extensionName = ArgumentType.String("extensionName");

        setArgumentCallback(this::extensionCallback, extensionName);

        addSyntax(this::execute, extensionName);
    }

    private void usage(CommandSender sender, CommandContext context) {
        sender.sendMessage(Component.text("Usage: /unload <extension name>"));
    }

    private void execute(CommandSender sender, CommandContext context) {
        final String name = context.get(extensionName);
        sender.sendMessage(Component.text("extensionName = " + name + "...."));

        ExtensionManager extensionManager = MinecraftServer.getExtensionManager();
        Extension ext = extensionManager.getExtension(name);
        if (ext != null) {
            try {
                extensionManager.unloadExtension(name);
            } catch (Throwable t) {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    t.printStackTrace();
                    t.printStackTrace(new PrintStream(baos));
                    baos.flush();
                    baos.close();
                    String contents = baos.toString(StandardCharsets.UTF_8);
                    contents.lines().map(Component::text).forEach(sender::sendMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            sender.sendMessage(Component.text("Extension '" + name + "' does not exist."));
        }
    }

    private void extensionCallback(CommandSender sender, ArgumentSyntaxException exception) {
        sender.sendMessage(Component.text("'" + exception.getInput() + "' is not a valid extension name!"));
    }
}
