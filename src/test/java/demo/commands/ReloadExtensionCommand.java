package demo.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.extensions.Extension;
import net.minestom.server.extensions.ExtensionManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class ReloadExtensionCommand extends Command {

    // the extensions name as an array
    private static String[] extensionsName;

    static {
        ReloadExtensionCommand.extensionsName = MinecraftServer.getExtensionManager().getExtensions()
                .stream()
                .map(extension -> extension.getDescription().getName())
                .toArray(String[]::new);
    }

    public ReloadExtensionCommand() {
        super("reload");

        setDefaultExecutor(this::usage);

        Argument extension = ArgumentType.DynamicStringArray("extensionName");

        setArgumentCallback(this::gameModeCallback, extension);

        addSyntax(this::execute, extension);
    }

    private void usage(CommandSender sender, CommandContext context) {
        sender.sendMessage("Usage: /reload <extension name>");
    }

    private void execute(CommandSender sender, CommandContext context) {
        String name = join(context.getStringArray("extensionName"));
        sender.sendMessage("extensionName = " + name + "....");

        ExtensionManager extensionManager = MinecraftServer.getExtensionManager();
        Extension ext = extensionManager.getExtension(name);
        if (ext != null) {
            try {
                extensionManager.reload(name);
            } catch (Throwable t) {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    t.printStackTrace();
                    t.printStackTrace(new PrintStream(baos));
                    baos.flush();
                    baos.close();
                    String contents = new String(baos.toByteArray(), StandardCharsets.UTF_8);
                    contents.lines().forEach(sender::sendMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            sender.sendMessage("Extension '" + name + "' does not exist.");
        }
    }

    private void gameModeCallback(CommandSender sender, ArgumentSyntaxException argumentSyntaxException) {
        sender.sendMessage("'" + argumentSyntaxException.getInput() + "' is not a valid extension name!");
    }

    @Nullable
    @Override
    public String[] onDynamicWrite(@NotNull CommandSender sender, @NotNull String text) {
        return extensionsName;
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
