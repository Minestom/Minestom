package net.minestom.server.command.builder.arguments.relative;

import net.minestom.server.command.builder.arguments.Argument;
import org.jetbrains.annotations.NotNull;

/**
 * Common interface for all the relative location arguments.
 *
 * @param <T> the relative location type
 */
public abstract class ArgumentRelative<T> extends Argument<T> {

    public static final String RELATIVE_CHAR = "~";

    public static final int INVALID_NUMBER_COUNT_ERROR = 1;
    public static final int INVALID_NUMBER_ERROR = 2;

    private final int numberCount;

    public ArgumentRelative(@NotNull String id, int numberCount) {
        super(id, true);
        this.numberCount = numberCount;
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
