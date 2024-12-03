package net.minestom.server.command.builder.arguments.relative;

import net.minestom.server.command.ArgumentParserType;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Represents a block position with 3 integers (x;y;z) which can take relative coordinates.
 * <p>
 * Example: 5 ~ -3
 */
public class ArgumentRelativeBlockPosition extends ArgumentRelativeVec {

    public ArgumentRelativeBlockPosition(@NotNull String id) {
        super(id, 3);
    }

    @Override
    public ArgumentParserType parser() {
        return ArgumentParserType.BLOCK_POS;
    }

    @Override
    public String toString() {
        return String.format("RelativeBlockPosition<%s>", getId());
    }

    @Override
    Function<String, ? extends Number> getRelativeNumberParser() {
        return Double::parseDouble;
    }

    @Override
    Function<String, ? extends Number> getAbsoluteNumberParser() {
        return Integer::parseInt;
    }
}
