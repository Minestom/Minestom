package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.CommandReader;
import net.minestom.server.utils.StringUtils;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an argument which will take all the remaining of the command.
 * <p>
 * Example: Hey I am a string
 */
public class ArgumentStringArray extends Argument<String[]> {
    //todo maybe deprecate? this arg doesn't make sense, the user isn't a baby they can cut it themselves
    public static final byte[] prop = BinaryWriter.makeArray(packetWriter -> {
        packetWriter.writeVarInt(2); // Greedy phrase
    });

    public ArgumentStringArray(String id) {
        super(id);
    }

    @Override
    public @NotNull Result<String[]> parse(CommandReader reader) {
        return Result.success(reader.readRemaining().split(StringUtils.SPACE));
    }

    @Override
    public String parser() {
        return "brigadier:string";
    }

    @Override
    public byte @Nullable [] nodeProperties() {
        return prop;
    }

    @Override
    public String toString() {
        return String.format("StringArray<%s>", getId());
    }
}
