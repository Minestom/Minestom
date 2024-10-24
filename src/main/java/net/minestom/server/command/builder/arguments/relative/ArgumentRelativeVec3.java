package net.minestom.server.command.builder.arguments.relative;

import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Represents a {@link Vec} with 3 floating numbers (x;y;z) which can take relative coordinates.
 * <p>
 * Example: -1.2 ~ 5
 */
public class ArgumentRelativeVec3 extends ArgumentRelativeVec {

    public ArgumentRelativeVec3(@NotNull String id) {
        super(id, 3);
    }

    @Override
    public ArgumentParserType parser() {
        return ArgumentParserType.VEC3;
    }

    @Override
    public String toString() {
        return String.format("RelativeVec3<%s>", getId());
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
