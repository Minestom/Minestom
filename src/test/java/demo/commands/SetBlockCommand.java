package demo.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.location.RelativeVec;

import static net.minestom.server.command.builder.arguments.ArgumentType.BlockState;
import static net.minestom.server.command.builder.arguments.ArgumentType.RelativeVec3;

public class SetBlockCommand extends Command {
    public SetBlockCommand() {
        super("setblock");

        addSyntax((sender, context) -> {
            RelativeVec relativeVec = context.get("position");
            Block block = context.get("block");
            final Player player = sender.asPlayer();
            player.getInstance().setBlock(relativeVec.from(player), block);
        }, RelativeVec3("position"), BlockState("block"));
    }
}
