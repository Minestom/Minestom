package net.minestom.demo.commands;

import net.minestom.demo.block.TestBlockHandler;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentBlockState;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeBlockPosition;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.batch.AbsoluteBlockBatch;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.command.builder.arguments.ArgumentType.BlockState;
import static net.minestom.server.command.builder.arguments.ArgumentType.RelativeBlockPosition;

public class FillCommand extends Command {


    // Copy of the setblock command but for a range.
    public FillCommand() {
        super("fill");

        final ArgumentRelativeBlockPosition position1 = RelativeBlockPosition("position1");
        final ArgumentRelativeBlockPosition position2 = RelativeBlockPosition("position2");
        final ArgumentBlockState block = BlockState("block");

        addSyntax((sender, context) -> {
            final Player player = (Player) sender;

            final Point blockPosition1 = context.get(position1).from(player);
            final Point blockPosition2 = context.get(position2).from(player);

            AbsoluteBlockBatch batch = new AbsoluteBlockBatch();


            Block blockToPlace = context.get(block);
            if (blockToPlace.stateId() == Block.GOLD_BLOCK.stateId())
                blockToPlace = blockToPlace.withHandler(TestBlockHandler.INSTANCE);

            for (int x = Math.min(blockPosition1.blockX(), blockPosition2.blockX()); x <= Math.max(blockPosition1.blockX(), blockPosition2.blockX()); x++) {
                for (int y = Math.min(blockPosition1.blockY(), blockPosition2.blockY()); y <= Math.max(blockPosition1.blockY(), blockPosition2.blockY()); y++) {
                    for (int z = Math.min(blockPosition1.blockZ(), blockPosition2.blockZ()); z <= Math.max(blockPosition1.blockZ(), blockPosition2.blockZ()); z++) {
                        batch.setBlock(x, y, z, blockToPlace);
                    }
                }
            }

            batch.apply(player.getInstance(), ()-> {
                player.sendMessage("Completed");
            });
        }, position1, position2, block);
    }
}
