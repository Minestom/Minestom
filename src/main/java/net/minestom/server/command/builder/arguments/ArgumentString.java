package net.minestom.server.command.builder.arguments;

import io.netty.util.internal.StringUtil;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Argument which will take a quoted string.
 * <p>
 * Example: "Hey I am a string"
 */
public class ArgumentString extends Argument<String> {

    public static final char BACKSLASH = '\\';

    public static final int QUOTE_ERROR = 1;

    public ArgumentString(String id) {
        super(id, true);
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
        argumentNode.properties = packetWriter -> {
            packetWriter.writeVarInt(1); // Quotable phrase
        };

        nodeMaker.addNodes(new DeclareCommandsPacket.Node[]{argumentNode});
    }

    @NotNull
    public static String staticParse(@NotNull String input) throws ArgumentSyntaxException {
        // Check if value start and end with quote
        final char first = input.charAt(0);
        final char last = input.charAt(input.length() - 1);
        final boolean quote = first == StringUtil.DOUBLE_QUOTE && last == StringUtil.DOUBLE_QUOTE;
        if (!quote)
            throw new ArgumentSyntaxException("String argument needs to start and end with quotes", input, QUOTE_ERROR);

        // Remove first and last characters (quotes)
        input = input.substring(1, input.length() - 1);

        // Verify backslashes
        for (int i = 1; i < input.length(); i++) {
            final char c = input.charAt(i);
            if (c == StringUtil.DOUBLE_QUOTE) {
                final char lastChar = input.charAt(i - 1);
                if (lastChar != BACKSLASH) {
                    throw new ArgumentSyntaxException("Non-escaped quote", input, QUOTE_ERROR);
                }
            }
        }

        return StringEscapeUtils.unescapeJava(input);
    }
}
