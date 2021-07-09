package net.minestom.server.command.builder.arguments.relative;

import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.utils.StringUtils;
import net.minestom.server.utils.location.RelativeVec;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Common interface for all the relative location arguments.
 */
public abstract class ArgumentRelativeVec extends Argument<RelativeVec> {

    public static final String RELATIVE_CHAR = "~";

    public static final int INVALID_NUMBER_COUNT_ERROR = 1;
    public static final int INVALID_NUMBER_ERROR = 2;

    private final int numberCount;

    public ArgumentRelativeVec(@NotNull String id, int numberCount) {
        super(id, true);
        this.numberCount = numberCount;
    }

    abstract Function<String, ? extends Number> getRelativeNumberParser();
    abstract Function<String, ? extends Number> getAbsoluteNumberParser();

    @NotNull
    @Override
    public RelativeVec parse(@NotNull String input) throws ArgumentSyntaxException {
        final String[] split = input.split(StringUtils.SPACE);
        if (split.length != getNumberCount()) {
            throw new ArgumentSyntaxException("Invalid number of values", input, INVALID_NUMBER_COUNT_ERROR);
        }
        return RelativeVec.parse(split, getRelativeNumberParser(), getAbsoluteNumberParser());
    }

    /**
     * Gets the amount of numbers that this relative location needs.
     *
     * @return the amount of coordinate required
     */
    public int getNumberCount() {
        return numberCount;
    }
}
