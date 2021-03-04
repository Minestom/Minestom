package demo.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.chat.JsonMessage;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

public class TitleCommand extends Command {
    public TitleCommand() {
        super("title");
        setDefaultExecutor((source, args) -> {
            source.sendMessage(Component.text("Unknown syntax (note: title must be quoted)"));
        });

        var content = ArgumentType.String("content");

        addSyntax(this::handleTitle, content);
    }

    private void handleTitle(CommandSender source, CommandContext context) {
        if (!source.isPlayer()) {
            source.sendMessage(Component.text("Only players can run this command!"));
            return;
        }

        Player player = source.asPlayer();
        String titleContent = context.get("content");

        player.showTitle(Title.title(Component.text(titleContent), Component.empty(), Title.DEFAULT_TIMES));
    }
}
