package demo.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentPotionEffect;
import net.minestom.server.command.builder.arguments.number.ArgumentNumber;
import net.minestom.server.entity.Player;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;

public class PotionCommand extends Command {

    private static final ArgumentPotionEffect potionArg = ArgumentType.Potion("potion");
    private static final ArgumentNumber<Integer> durationArg = ArgumentType.Integer("duration");

    public PotionCommand() {
        super("potion");

        setCondition(this::condition);

        setDefaultExecutor(((sender, args) -> {
            sender.sendMessage("Usage: /potion [type] [duration (seconds)]");
        }));

        addSyntax(this::onPotionCommand, potionArg, durationArg);
    }

    private boolean condition(CommandSender sender, String commandString) {
        if (!sender.isPlayer()) {
            sender.sendMessage("The command is only available for players");
            return false;
        }
        return true;
    }

    private void onPotionCommand(CommandSender sender, Arguments args) {
        final Player player = (Player) sender;
        final PotionEffect potion = args.get(potionArg);
        final int duration = args.get(durationArg);

        player.sendMessage(player.getActiveEffects().toString());
        player.addEffect(new Potion(
                potion,
                (byte) 0,
                duration * MinecraftServer.TICK_PER_SECOND
        ));
    }

}