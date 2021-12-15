package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.nbt.SNBTParser;

import java.io.StringReader;

/**
 * Argument used to retrieve a {@link NBT} based object, can be any kind of tag like
 * {@link org.jglrxavpok.hephaistos.nbt.NBTCompound}, {@link org.jglrxavpok.hephaistos.nbt.NBTList},
 * {@link org.jglrxavpok.hephaistos.nbt.NBTInt}, etc...
 * <p>
 * Example: {display:{Name:"{\"text\":\"Sword of Power\"}"}} or [{display:{Name:"{\"text\":\"Sword of Power\"}"}}]
 */
public class ArgumentNbtTag extends Argument<NBT> {

    public static final int INVALID_NBT = 1;

    public ArgumentNbtTag(@NotNull String id) {
        super(id);
    }

    @Override
    public @NotNull NBT parse(@NotNull net.minestom.server.command.StringReader input) throws CommandException {
        // FIXME: This has not been implemented because Hephaistos does not support reading select amounts of a reader yet.
        throw CommandException.COMMAND_UNKNOWN_ARGUMENT.generateException(input);
    }

    @NotNull
    @Override
    public NBT parse(@NotNull String input) throws ArgumentSyntaxException {
        try {
            return new SNBTParser(new StringReader(input)).parse();
        } catch (NBTException e) {
            throw new ArgumentSyntaxException("Invalid NBT", input, INVALID_NBT);
        }
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);
        argumentNode.parser = "minecraft:nbt_tag";

        nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{argumentNode});
    }

    @Override
    public String toString() {
        return String.format("NBT<%s>", getId());
    }
}
