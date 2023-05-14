package net.minestom.demo.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeBlockPosition;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.batch.RelativeBlockBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.location.RelativeVec;

import org.jetbrains.annotations.NotNull;

public class DebugGridCommand extends Command {
    private final Argument<RelativeVec> center = new ArgumentRelativeBlockPosition("center")
            .setDefaultValue(new RelativeVec(new Vec(0, -1, 0), RelativeVec.CoordinateType.RELATIVE, true, true, true));
    private final Argument<Integer> radius = new ArgumentInteger("radius")
            .setDefaultValue(100);

    public DebugGridCommand() {
        super("dg");
        setCondition(Conditions::playerOnly);
        addSyntax(this::execute, radius, center);
    }

    private void execute(@NotNull CommandSender sender, @NotNull CommandContext context) {
        Player player = (Player) sender;
        final RelativeBlockBatch relativeBlockBatch = new RelativeBlockBatch();
        final Integer radius = context.get(this.radius);
        for (int x = -radius / 2; x < radius / 2; x++) {
            for (int z = -radius / 2; z < radius / 2; z++) {
                relativeBlockBatch.setBlock(x, 0, z, ((x % 2 == 0) ^ (z % 2) == 0) ? Block.WHITE_CONCRETE : Block.BLACK_CONCRETE);
            }
        }
        //noinspection ConstantConditions
        relativeBlockBatch.apply(player.getInstance(), context.get(center).from(player), () -> {});
    }
}
