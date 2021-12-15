package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.utils.StringUtils;
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
         * Reads a quoted string from the provided reader via {@link StringReader#readQuotedString()}.
         */
        QUOTED,

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

    private static final char BACKSLASH = '\\';
    private static final char DOUBLE_QUOTE = '"';
    private static final char QUOTE = '\'';

    public static final int QUOTE_ERROR = 1;

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
            case QUOTED -> input.readQuotedString();
            case UNQUOTED -> input.readUnquotedString();
            case NORMAL -> input.readString();
            case GREEDY -> input.readAll();
        };
    }

    @NotNull
    @Override
    public String parse(@NotNull String input) throws ArgumentSyntaxException {
        return staticParse(input);
    }

    @Override
    public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(this, executable, false, false);

        argumentNode.parser = "brigadier:string";
        argumentNode.properties = BinaryWriter.makeArray(packetWriter -> {
            packetWriter.writeVarInt(1); // Quotable phrase
        });

        nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{argumentNode});
    }

    /**
     * @deprecated use {@link Argument#parse(Argument)}
     */
    @Deprecated
    public static String staticParse(@NotNull String input) throws ArgumentSyntaxException {
        // Return if not quoted
        if (!input.contains(String.valueOf(DOUBLE_QUOTE)) &&
                !input.contains(String.valueOf(QUOTE)) &&
                !input.contains(StringUtils.SPACE)) {
            return input;
        }

        // Check if value start and end with quote
        final char first = input.charAt(0);
        final char last = input.charAt(input.length() - 1);
        final boolean quote = input.length() >= 2 &&
                first == last && (first == DOUBLE_QUOTE || first == QUOTE);
        if (!quote)
            throw new ArgumentSyntaxException("String argument needs to start and end with quotes", input, QUOTE_ERROR);

        // Remove first and last characters (quotes)
        input = input.substring(1, input.length() - 1);

        // Verify backslashes
        for (int i = 1; i < input.length(); i++) {
            final char c = input.charAt(i);
            if (c == first) {
                final char lastChar = input.charAt(i - 1);
                if (lastChar != BACKSLASH) {
                    throw new ArgumentSyntaxException("Non-escaped quote", input, QUOTE_ERROR);
                }
            }
        }

        return StringUtils.unescapeJavaString(input);
    }

    @Override
    public String toString() {
        return String.format("String<%s>", getId());
    }
}
