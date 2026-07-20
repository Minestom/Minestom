package net.minestom.demo.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.scoreboard.DisplaySlot;
import net.minestom.server.scoreboard.Objective;

import static net.minestom.server.command.builder.arguments.ArgumentType.Literal;

public class BelowNameCommand extends Command {

    private final ArgumentEntity target = ArgumentType.Entity("target").onlyPlayers(true).singleEntity(true);
    private final Argument<Integer> value = ArgumentType.Integer("value");

    public BelowNameCommand() {
        super("belowname");

        Objective objective = Objective.create("belowname-test", Component.text("lorum"));

        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            Player targetPlayer = context.get(target).findFirstPlayer(player);
            if (targetPlayer == null) return;
            player.setDisplayedObjective(DisplaySlot.BELOW_NAME, objective);
            objective.updateScore(targetPlayer, context.get(value));
        }, Literal("set"), target, value);

        addSyntax((sender, context) -> {
            if (!(sender instanceof Player player)) return;
            player.setDisplayedObjective(DisplaySlot.BELOW_NAME, null);
        }, Literal("clear"));
    }
}
