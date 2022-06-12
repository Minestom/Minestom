package net.minestom.demo.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentBoolean;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

public class ChatPreviewCommand extends Command {
    private static final ArgumentBoolean on = ArgumentType.Boolean("on");

    public ChatPreviewCommand() {
        super("preview");

        addSyntax(((sender, context) -> {
            if (sender instanceof Player player) {
                player.toggleChatPreview(context.get(on));
            }
        }), on);
    }
}
