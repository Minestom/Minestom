package demo.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.extensions.Extension;
import net.minestom.server.extensions.ExtensionManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class UnloadExtensionCommand extends Command {

    private final Argument<Extension> extensionName;

    public UnloadExtensionCommand() {
        super("unload");

        setDefaultExecutor(this::usage);

        extensionName = ArgumentType.String("extensionName").map((input) -> {
            Extension extension = MinecraftServer.getExtensionManager().getExtension(input);

            if (extension == null) throw new ArgumentSyntaxException("The specified extension was not found", input, 1);

            return extension;
        });

        setArgumentCallback(this::extensionCallback, extensionName);

        addSyntax(this::execute, extensionName);
    }

    private void usage(CommandSender sender, CommandContext context) {
        sender.sendMessage(Component.text("Usage: /unload <extension name>"));
    }

    private void execute(CommandSender sender, CommandContext context) {
        final Extension ext = context.get(extensionName);
        sender.sendMessage(Component.text("extensionName = " + ext.getOrigin().getName() + "...."));

        ExtensionManager extensionManager = MinecraftServer.getExtensionManager();

        try {
            extensionManager.unloadExtension(ext.getOrigin().getName());
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
    }

    private void extensionCallback(CommandSender sender, ArgumentSyntaxException exception) {
        sender.sendMessage(Component.text("'" + exception.getInput() + "' is not a valid extension name!"));
    }
}
