package demo.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.animal.HorseMeta;

import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HorseCommand extends Command {

    public HorseCommand() {
        super("horse");
        setCondition(this::condition);
        setDefaultExecutor(this::defaultExecutor);
        var babyArg = ArgumentType.Boolean("baby");
        var markingArg = ArgumentType.Enum("marking", HorseMeta.Marking.class);
        var colorArg = ArgumentType.Enum("color", HorseMeta.Color.class);
        setArgumentCallback(this::onBabyError, babyArg);
        setArgumentCallback(this::onMarkingError, markingArg);
        setArgumentCallback(this::onColorError, colorArg);
        addSyntax(this::onHorseCommand, babyArg, markingArg, colorArg);
    }

    private boolean condition(CommandSender sender, String commandString) {
        if (!sender.isPlayer()) {
            sender.sendMessage("The command is only available for player");
            return false;
        }
        return true;
    }

    private void defaultExecutor(CommandSender sender, Arguments args) {
        sender.sendMessage("Correct usage: horse [baby] [marking] [color]");
    }

    private void onBabyError(CommandSender sender, ArgumentSyntaxException exception) {
        sender.sendMessage("SYNTAX ERROR: '" + exception.getInput() + "' should be replaced by 'true' or 'false'");
    }

    private void onMarkingError(CommandSender sender, ArgumentSyntaxException exception) {
        String values = Stream.of(HorseMeta.Marking.values())
                .map(value -> "'" + value.name().toLowerCase(Locale.ROOT) + "'")
                .collect(Collectors.joining(", "));
        sender.sendMessage("SYNTAX ERROR: '" + exception.getInput() + "' should be replaced by " + values + ".");
    }

    private void onColorError(CommandSender sender, ArgumentSyntaxException exception) {
        String values = Stream.of(HorseMeta.Color.values())
                .map(value -> "'" + value.name().toLowerCase(Locale.ROOT) + "'")
                .collect(Collectors.joining(", "));
        sender.sendMessage("SYNTAX ERROR: '" + exception.getInput() + "' should be replaced by " + values + ".");
    }

    private void onHorseCommand(CommandSender sender, Arguments args) {
        var player = (Player) sender;

        boolean baby = args.get("baby");
        HorseMeta.Marking marking = args.get("marking");
        HorseMeta.Color color = args.get("color");
        var horse = new EntityCreature(EntityType.HORSE, player.getPosition());
        var meta = (HorseMeta) horse.getEntityMeta();
        meta.setBaby(baby);
        meta.setVariant(new HorseMeta.Variant(marking, color));
        horse.setInstance(player.getInstance());
    }

}
