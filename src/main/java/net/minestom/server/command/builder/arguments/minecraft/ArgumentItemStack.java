package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.utils.NBTUtils;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

/**
 * Argument which can be used to retrieve an {@link ItemStack} from its material and with NBT data.
 * <p>
 * It is the same type as the one used in the /give command.
 * <p>
 * Example: diamond_sword{display:{Name:"{\"text\":\"Sword of Power\"}"}}
 */
public class ArgumentItemStack extends Argument<ItemStack> {

    public static final int TAG_MARKER = '#';

    public ArgumentItemStack(@NotNull String id) {
        super(id);
    }

    @Override
    public @NotNull ItemStack parse(@NotNull StringReader input) throws CommandException {
        return read(input);
    }

    public static @NotNull ItemStack read(@NotNull StringReader input) throws CommandException {
        int start = input.position();
        if (input.canRead() && input.peek() == TAG_MARKER) {
            throw CommandException.ARGUMENT_ITEM_TAG_DISALLOWED.generateException(input.all(), start);
        }
        NamespaceID id = input.readNamespaceID();
        Material material = Material.fromNamespaceId(id);
        if (material == null) {
            throw CommandException.ARGUMENT_ITEM_ID_INVALID.generateException(input.all(), start, id.asString());
        }
        if (input.canRead() && input.peek() == '{') {
            @SuppressWarnings("deprecation")
            NBT nbt = NBTUtils.readSNBT(input);
            if (!(nbt instanceof NBTCompound compound)) {
                input.position(start);
                throw CommandException.COMMAND_UNKNOWN_ARGUMENT.generateException(input.all(), start);
            }
            return ItemStack.fromNBT(material, compound);
        }
        return ItemStack.of(material);
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
