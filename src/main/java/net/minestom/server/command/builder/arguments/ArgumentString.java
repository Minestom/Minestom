package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.CommandReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Argument which will take a quoted string.
 * <p>
 * Example: "Hey I am a string"
 */
public class ArgumentString extends Argument<String> {
    private static final char DOUBLE_QUOTE = '"';
    private static final char QUOTE = '\'';

    public static final int QUOTE_ERROR = 1;

    public ArgumentString(String id) {
        super(id);
    }

    @Override
    public @NotNull Result<String> parse(CommandReader reader) {
        //todo check if we should support '
        final char c = reader.peekNextChar();
        if (c == DOUBLE_QUOTE) {
            try {
                return Result.success(reader.readQuotedString());
            } catch (Exception e) {
                return Result.syntaxError("String doesn't have closing quotes", reader.readRemaining(), QUOTE_ERROR);
            }
        } else {
            return Result.success(reader.readWord());
        }
    }

    @Override
    public String parser() {
        return "brigadier:string";
    }

    @Override
    public byte @Nullable [] nodeProperties() {
        return BinaryWriter.makeArray(packetWriter -> {
            packetWriter.writeVarInt(1); // Quotable phrase
        });
    }

    @Override
    public String toString() {
        return String.format("String<%s>", getId());
    }
}
