package net.minestom.demo.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;

public class TitleCommand extends Command {
    public TitleCommand() {
        super("title");
        setDefaultExecutor((source, args) -> source.sendMessage(Component.text("Unknown syntax (note: title must be quoted)")));
        setCondition(Conditions::playerOnly);

        var content = ArgumentType.String("content");

        addSyntax(this::handleTitle, content);
    }

    private void handleTitle(CommandSender source, CommandContext context) {
        Player player = (Player) source;
        String titleContent = context.get("content");

        player.showTitle(Title.title(Component.text(titleContent), Component.empty(), Title.DEFAULT_TIMES));
    }
}
