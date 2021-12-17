package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

/**
 * Argument used to retrieve a {@link NBTCompound} if you need key-value data.
 * <p>
 * Example: {display:{Name:"{\"text\":\"Sword of Power\"}"}}
 */
public class ArgumentNbtCompoundTag extends Argument<NBTCompound> {

    public ArgumentNbtCompoundTag(@NotNull String id) {
        super(id);
    }

    @Override
    public @NotNull NBTCompound parse(@NotNull StringReader input) throws CommandException {
        // FIXME: This has not been implemented because Hephaistos does not support reading select amounts of a reader yet.
        throw CommandException.COMMAND_UNKNOWN_ARGUMENT.generateException(input);
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
        argumentNode.parser = "minecraft:nbt_compound_tag";

        nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{argumentNode});
    }

    @Override
    public String toString() {
        return String.format("NbtCompound<%s>", getId());
    }
}
