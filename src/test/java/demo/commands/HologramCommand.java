package demo.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.hologram.Hologram;

public class HologramCommand extends Command {
    public HologramCommand() {
        super("hologram");
        setDefaultExecutor((source, args) -> source.sendMessage(Component.text("Unknown syntax (note: title must be quoted)")));
        setCondition(Conditions::playerOnly);

        var content = ArgumentType.String("content");
        var count = ArgumentType.Integer("count");

        addSyntax(this::handleHologram, content);
        addSyntax(this::handleHologramWithCount, content, count);
    }

    private void handleHologram(CommandSender source, CommandContext context) {
        Player player = source.asPlayer();
        String hologramContent = context.get("content");

        new Hologram(player.getInstance(), player.getPosition(), Component.text(hologramContent));
    }

    private void handleHologramWithCount(CommandSender source, CommandContext context) {
        Player player = source.asPlayer();
        String hologramContent = context.get("content");
        Integer hologramCount = context.get("count");

        for (int i = 0; i < hologramCount; i++)
        {
            new Hologram(player.getInstance(), player.getPosition(), Component.text(hologramContent));
        }
    }
}
