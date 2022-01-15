package demo.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandOrigin;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.animal.HorseMeta;
import org.jetbrains.annotations.NotNull;

public class HorseCommand extends Command {

    public HorseCommand() {
        super("horse");
        setCondition(Conditions::playerOnly);
        setDefaultExecutor(this::defaultExecutor);
        var babyArg = ArgumentType.Boolean("baby");
        var markingArg = ArgumentType.Enum("marking", HorseMeta.Marking.class);
        var colorArg = ArgumentType.Enum("color", HorseMeta.Color.class);
        setArgumentCallback(CommandException.STANDARD_CALLBACK, babyArg);
        setArgumentCallback(this::onMarkingError, markingArg);
        setArgumentCallback(this::onColorError, colorArg);
        addSyntax(this::onHorseCommand, babyArg, markingArg, colorArg);
    }

    private void defaultExecutor(@NotNull CommandOrigin origin, @NotNull CommandContext context) {
        origin.sender().sendMessage(Component.text("Correct usage: /horse <baby> <marking> <color>"));
    }

    private void onMarkingError(@NotNull CommandOrigin origin, @NotNull CommandException exception) {
        origin.sender().sendMessage(Component.text("Expected a valid horse marking", NamedTextColor.RED));
        origin.sender().sendMessage(exception.generateContextMessage());
    }

    private void onColorError(@NotNull CommandOrigin origin, @NotNull CommandException exception) {
        origin.sender().sendMessage(Component.text("Expected a valid horse color", NamedTextColor.RED));
        origin.sender().sendMessage(exception.generateContextMessage());
    }

    private void onHorseCommand(@NotNull CommandOrigin origin, @NotNull CommandContext context) {
        var player = (Player) origin.entity();

        boolean baby = context.get("baby");
        HorseMeta.Marking marking = context.get("marking");
        HorseMeta.Color color = context.get("color");
        var horse = new EntityCreature(EntityType.HORSE);
        var meta = (HorseMeta) horse.getEntityMeta();
        meta.setBaby(baby);
        meta.setVariant(new HorseMeta.Variant(marking, color));
        //noinspection ConstantConditions - It should be impossible to execute a command without being in an instance
        horse.setInstance(player.getInstance(), player.getPosition());
    }

}
