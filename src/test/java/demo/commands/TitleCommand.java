package demo.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.minestom.server.command.CommandOrigin;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TitleCommand extends Command {
    public TitleCommand() {
        super("title");
        setDefaultExecutor((origin, context) -> origin.sender().sendMessage(Component.text("Unknown syntax (note: title must be quoted)")));
        setCondition(Conditions::playerOnly);

        var content = ArgumentType.String("content");

        addSyntax(this::handleTitle, content);
    }

    private void handleTitle(@NotNull CommandOrigin origin, @NotNull CommandContext context) {
        Player player = (Player) origin.entity();
        String titleContent = context.get("content");

        player.showTitle(Title.title(Component.text(titleContent), Component.empty(), Title.DEFAULT_TIMES));
    }
}
