package demo.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandOrigin;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.extensions.Extension;
import net.minestom.server.extensions.ExtensionManager;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class UnloadExtensionCommand extends Command {

    private final Argument<Extension> extensionName;

    public UnloadExtensionCommand() {
        super("unload");

        setDefaultExecutor(this::usage);

        extensionName = ArgumentType.String("extensionName").map(MinecraftServer.getExtensionManager()::getExtension);

        setArgumentCallback(this::extensionCallback, extensionName);

        addSyntax(this::execute, extensionName);
    }

    private void usage(@NotNull CommandOrigin origin, @NotNull CommandContext context) {
        origin.sender().sendMessage(Component.text("Usage: /unload <extension name>"));
    }

    private void execute(@NotNull CommandOrigin origin, @NotNull CommandContext context) {
        final Extension ext = context.get(extensionName);
        if (ext == null) {
            origin.sender().sendMessage(Component.text("Invalid extension!", NamedTextColor.RED));
            return;
        }
        origin.sender().sendMessage(Component.text("extensionName = " + ext.getOrigin().getName() + "...."));

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
                contents.lines().map(Component::text).forEach(origin.sender()::sendMessage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void extensionCallback(@NotNull CommandOrigin origin, @NotNull CommandException exception) {
        origin.sender().sendMessage(Component.text("Expected a valid extension name", NamedTextColor.RED));
        origin.sender().sendMessage(exception.generateContextMessage());
    }
}
