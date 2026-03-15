package net.minestom.demo.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.scoreboard.BelowNameTag;
import net.minestom.server.scoreboard.Scoreboard;

import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;

public class BelowNameCommand extends Command {

    private final ArgumentEntity target = ArgumentType.Entity("target").onlyPlayers(true).singleEntity(true);
    private final Argument<Integer> value = ArgumentType.Integer("value");

    public BelowNameCommand() {
        super("belowname");

        Scoreboard belowNameTag = Scoreboard.create("test", Scoreboard.Position.BELOW_NAME);
        belowNameTag.setDisplayName(Component.text("lorem"));

        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            Player targetPlayer = context.get(target).findFirstPlayer(player);
            if (targetPlayer == null) return;
            belowNameTag.addViewer(player);
            Integer targetValue = context.get(value);
            belowNameTag.updateScore(targetPlayer, targetValue);
        }, Literal("set"), target, value);
    }
}
