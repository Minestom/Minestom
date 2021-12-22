package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.utils.NBTUtils;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
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
        NBT nbt = NBTUtils.readSNBT(input);

        // FIXME: Throw an exception that's more accurate (ideally once Hephaistos adds partial reading)
        if (!(nbt instanceof NBTCompound compound)) {
            throw CommandException.COMMAND_UNKNOWN_ARGUMENT.generateException(input.all(), input.position());
        }

        return compound;
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
        argumentNode.parser = "minecraft:nbt_compound_tag";

        nodeMaker.addNodes(argumentNode);
    }

    @Override
    public String toString() {
        return String.format("NbtCompound<%s>", getId());
    }
}
