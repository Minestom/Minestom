package net.minestom.demo.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentBlockState;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeBlockPosition;
import net.minestom.server.entity.Player;

import static net.minestom.server.command.builder.arguments.ArgumentType.BlockState;
import static net.minestom.server.command.builder.arguments.ArgumentType.RelativeBlockPosition;

public class SetBlockCommand extends Command {
    public SetBlockCommand() {
        super("setblock");

        final ArgumentRelativeBlockPosition position = RelativeBlockPosition("position");
        final ArgumentBlockState block = BlockState("block");

        addSyntax((sender, context) -> {
            final Player player = (Player) sender;
            player.getInstance().setBlock(context.get(position).from(player), context.get(block));
        }, position, block);
    }
}
