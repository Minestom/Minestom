package net.minestom.server.command.builder.arguments;

import net.minestom.server.utils.StringUtils;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * Represents an argument which will take all the remaining of the command.
 * <p>
 * Example: Hey I am a string
 */
public class ArgumentStringArray extends Argument<String[]> {

    public ArgumentStringArray(String id) {
        super(id, true, true);
    }

    @NotNull
    @Override
    public String[] parse(@NotNull String input) {
        return input.split(Pattern.quote(StringUtils.SPACE));
    }

    @Override
    public String parser() {
        return "brigadier:string";
    }

    @Override
    public byte @Nullable [] nodeProperties() {
        return BinaryWriter.makeArray(packetWriter -> {
            packetWriter.writeVarInt(2); // Greedy phrase
        });
    }

    @Override
    public String toString() {
        return String.format("StringArray<%s>", getId());
    }
}
