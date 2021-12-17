package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Argument which will take a string.
 * <p>
 * Example: "Hey I am a string"
 */
public class ArgumentString extends Argument<String> {

    /**
     * Represents different ways of reading a string from provided {@code StringReader}s
     */
    public enum ReadType {

        /**
         * Reads an unquoted string from the provided reader via {@link StringReader#readUnquotedString()} ()}.
         */
        UNQUOTED,

        /**
         * Reads a quoted or unquoted string from the provided reader via {@link StringReader#readString()}.
         */
        NORMAL,

        /**
         * Reads and returns the entire rest of the string from the reader via {@link StringReader#readAll()}.
         */
        GREEDY
    }

    private ReadType readType = ReadType.NORMAL;

    public ArgumentString(@NotNull String id) {
        super(id);
    }

    @Contract("_ -> this")
    public @NotNull ArgumentString setReadType(@NotNull ReadType readType) {
        this.readType = readType;
        return this;
    }

    public @NotNull ReadType getReadType() {
        return readType;
    }

    @Override
    public @NotNull String parse(@NotNull StringReader input) throws CommandException {
        return switch(readType){
            case UNQUOTED -> input.readUnquotedString();
            case NORMAL -> input.readString();
            case GREEDY -> input.readAll();
        };
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);

        argumentNode.parser = "brigadier:string";
        argumentNode.properties = BinaryWriter.makeArray(packetWriter -> packetWriter.writeVarInt(
            switch(this.readType) {
                // Single word
                case UNQUOTED -> 0;
                // Quotable phrase
                case NORMAL -> 1;
                // Greedy phrase
                case GREEDY -> 2;
            }
        ));

        nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{argumentNode});
    }


    @Override
    public String toString() {
        return String.format("String<%s>", getId());
    }
}
