package demo.commands;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandOrigin;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentPotionEffect;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

public class PotionCommand extends Command {

    private final ArgumentPotionEffect potion;
    private final ArgumentInteger duration;

    public PotionCommand() {
        super("potion");

        setCondition(Conditions::playerOnly);

        setDefaultExecutor((origin, context) -> origin.sender().sendMessage(Component.text("Usage: /potion <type> <duration (seconds)>")));

        potion = ArgumentType.Potion("potion");
        duration = ArgumentType.Integer("duration");

        addSyntax(this::onPotionCommand, potion, duration);
    }

    private void onPotionCommand(@NotNull CommandOrigin origin, @NotNull CommandContext context) {
        final Player player = (Player) origin.entity();
        final PotionEffect potionEffect = context.get(potion);
        final Integer duration = context.get(this.duration);

        player.sendMessage(Component.text(player.getActiveEffects().toString()));
        player.addEffect(new Potion(
                potionEffect,
                (byte) 0,
                duration * MinecraftServer.TICK_PER_SECOND
        ));
    }

}