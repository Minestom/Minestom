package net.minestom.server.command.builder.arguments.relative;

import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Represents a {@link Vec} with 2 floating numbers (x;z) which can take relative coordinates.
 * <p>
 * Example: -1.2 ~
 */
public class ArgumentRelativeVec2 extends ArgumentRelativeVec {

    public ArgumentRelativeVec2(@NotNull String id) {
        super(id, 2);
    }

    @Override
    public ArgumentParserType parser() {
        return ArgumentParserType.VEC2;
    }

    @Override
    public String toString() {
        return String.format("RelativeVec2<%s>", getId());
    }

    @Override
    Function<String, ? extends Number> getRelativeNumberParser() {
        return Double::parseDouble;
    }

    @Override
    Function<String, ? extends Number> getAbsoluteNumberParser() {
        return Double::parseDouble;
    }
}
