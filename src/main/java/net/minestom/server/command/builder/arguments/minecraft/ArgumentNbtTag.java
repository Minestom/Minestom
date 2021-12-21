package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;

/**
 * Argument used to retrieve a {@link NBT} based object, can be any kind of tag like
 * {@link org.jglrxavpok.hephaistos.nbt.NBTCompound}, {@link org.jglrxavpok.hephaistos.nbt.NBTList},
 * {@link org.jglrxavpok.hephaistos.nbt.NBTInt}, etc...
 * <p>
 * Example: {display:{Name:"{\"text\":\"Sword of Power\"}"}} or [{display:{Name:"{\"text\":\"Sword of Power\"}"}}]
 */
public class ArgumentNbtTag extends Argument<NBT> {

    public ArgumentNbtTag(@NotNull String id) {
        super(id);
    }

    @Override
    public @NotNull NBT parse(@NotNull StringReader input) throws CommandException {
        // FIXME: This has not been implemented because Hephaistos does not support reading select amounts of a reader yet.
        throw CommandException.COMMAND_UNKNOWN_ARGUMENT.generateException(input.all(), input.position());
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
        argumentNode.parser = "minecraft:nbt_tag";

        nodeMaker.addNodes(argumentNode);
    }

    @Override
    public String toString() {
        return String.format("NBT<%s>", getId());
    }
}
