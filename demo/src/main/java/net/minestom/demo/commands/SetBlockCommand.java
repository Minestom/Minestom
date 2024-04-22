package net.minestom.demo.commands;

import net.minestom.demo.block.TestBlockHandler;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentBlockState;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeBlockPosition;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;

import static net.minestom.server.command.builder.arguments.ArgumentType.BlockState;
import static net.minestom.server.command.builder.arguments.ArgumentType.RelativeBlockPosition;

public class SetBlockCommand extends Command {
    public SetBlockCommand() {
        super("setblock");

        final ArgumentRelativeBlockPosition position = RelativeBlockPosition("position");
        final ArgumentBlockState block = BlockState("block");

        addSyntax((sender, context) -> {
            final Player player = (Player) sender;

            Block blockToPlace = context.get(block);
            if (blockToPlace.stateId() == Block.GOLD_BLOCK.stateId())
                blockToPlace = blockToPlace.withHandler(TestBlockHandler.INSTANCE);

            player.getInstance().setBlock(context.get(position).from(player), blockToPlace);
        }, position, block);
    }
}
