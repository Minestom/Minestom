package net.minestom.server.command.builder.arguments.minecraft;

import net.minestom.server.command.builder.arguments.Argument;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract class used by {@link ArgumentIntRange} and {@link ArgumentFloatRange}.
 *
 * @param <T> the type of the range
 */
public abstract class ArgumentRange<T> extends Argument<T> {

    public static final int FORMAT_ERROR = -1;

    public ArgumentRange(String id) {
        super(id);
    }

    @Override
    public int getConditionResult(@NotNull T value) {
        return SUCCESS;
    }
}
