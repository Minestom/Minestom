package demo.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.extensions.Extension;
import net.minestom.server.extensions.ExtensionManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ReloadExtensionCommand extends Command {
    public ReloadExtensionCommand() {
        super("reload");

        setDefaultExecutor(this::usage);

        Argument extension = ArgumentType.DynamicStringArray("extensionName");

        setArgumentCallback(this::gameModeCallback, extension);

        addSyntax(this::execute, extension);
    }

    private void usage(CommandSender sender, Arguments arguments) {
        sender.sendMessage("Usage: /reload <extension name>");
    }

    private void execute(CommandSender sender, Arguments arguments) {
        String name = join(arguments.getStringArray("extensionName"));
        sender.sendMessage("extensionName = "+name+"....");

        ExtensionManager extensionManager = MinecraftServer.getExtensionManager();
        Extension ext = extensionManager.getExtension(name);
        if(ext != null) {
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
            sender.sendMessage("Extension '"+name+"' does not exist.");
        }
    }

    private void gameModeCallback(CommandSender sender, String extension, int error) {
        sender.sendMessage("'" + extension + "' is not a valid extension name!");
    }

    private String join(String[] extensionNameParts) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < extensionNameParts.length; i++) {
            String s = extensionNameParts[i];
            if(i != 0) {
                b.append(" ");
            }
            b.append(s);
        }
        return b.toString();
    }
}
