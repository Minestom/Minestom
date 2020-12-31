package demo.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.minestom.server.chat.ColoredText;
import net.minestom.server.chat.JsonMessage;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

public class TitleCommand extends Command {
    public TitleCommand() {
        super("title");
        setDefaultExecutor((source, args) -> {
            source.sendMessage("Unknown syntax (note: title must be quoted)");
        });

        Argument content = ArgumentType.String("content");

        addSyntax(this::handleTitle, content);
    }

    private void handleTitle(CommandSender source, Arguments args) {
        if (!source.isPlayer()) {
            source.sendMessage("Only players can run this command!");
            return;
        }

        Player player = source.asPlayer();
        String titleContent = args.getString("content");

        player.sendTitleTime(10, 100, 10);
        try {
            JsonElement parsed = JsonParser.parseString(titleContent);
            JsonMessage message = new JsonMessage.RawJsonMessage(parsed.getAsJsonObject());
            player.sendTitleMessage(message);
        } catch (JsonParseException | IllegalStateException ignored) {
            player.sendTitleMessage(ColoredText.of(titleContent));
        }
    }
}
