package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.NotNull;

/**
 * Argument which can be used to retrieve an {@link ItemStack} from its material and with NBT data.
 * <p>
 * It is the same type as the one used in the /give command.
 * <p>
 * Example: diamond_sword{display:{Name:"{\"text\":\"Sword of Power\"}"}}
 */
public class ArgumentItemStack extends Argument<ItemStack> {

    public ArgumentItemStack(@NotNull String id) {
        super(id);
    }

    @Override
    public @NotNull ItemStack parse(@NotNull StringReader input) throws CommandException {
        // FIXME: This has not been implemented because Hephaistos does not support reading select amounts of a reader yet.
        throw CommandException.COMMAND_UNKNOWN_ARGUMENT.generateException(input);
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
        argumentNode.parser = "minecraft:item_stack";

        nodeMaker.addNodes(argumentNode);
    }

    @Override
    public String toString() {
        return String.format("ItemStack<%s>", getId());
    }
}
