package demo.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeBlockPosition;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.instance.batch.RelativeBlockBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.location.RelativeBlockPosition;
import org.jetbrains.annotations.NotNull;

public class DebugGridCommand extends Command {
    private final ArgumentRelativeBlockPosition center;
    private final ArgumentInteger radius;

    public DebugGridCommand() {
        super("dg");
        setCondition(Conditions::playerOnly);

        center = new ArgumentRelativeBlockPosition("center");
        center.setDefaultValue(new RelativeBlockPosition(new BlockPosition(0,-1,0), true, true, true));
        radius = new ArgumentInteger("radius");
        radius.setDefaultValue(100);

        addSyntax(this::execute, radius, center);
    }

    private void execute(@NotNull CommandSender sender, @NotNull CommandContext context) {
        final RelativeBlockBatch relativeBlockBatch = new RelativeBlockBatch();
        final Integer radius = context.get(this.radius);
        for (int x = -radius/2; x < radius/2; x++) {
            for (int z = -radius/2; z < radius/2; z++) {
                relativeBlockBatch.setBlock(x,0,z, ((x % 2 == 0) ^ (z % 2) == 0) ? Block.WHITE_CONCRETE : Block.BLACK_CONCRETE);
            }
        }
        //noinspection ConstantConditions
        relativeBlockBatch.apply(sender.asPlayer().getInstance(), context.get(center).from(sender.asPlayer()), () -> {});
    }
}
