package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.CommandReader;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArgumentLoop<T> extends Argument<List<T>> {

    public static final int INVALID_INPUT_ERROR = 1;

    private final List<Argument<T>> arguments = new ArrayList<>();

    @SafeVarargs
    public ArgumentLoop(@NotNull String id, @NotNull Argument<T>... arguments) {
        super(id);
        this.arguments.addAll(Arrays.asList(arguments));
    }

    public List<Argument<T>> arguments() {
        return arguments;
    }

    @Override
    public @NotNull Result<List<T>> parse(CommandReader reader) {
        final List<T> result = new ArrayList<>();

        while (reader.hasRemaining()) {
            for (Argument<T> argument : arguments) {
                final T value = argument.parse(reader).value();
                if (value != null) {
                    result.add(value);
                } else {
                    if (result.isEmpty()) {
                        return Result.incompatibleType();
                    } else {
                        return Result.syntaxError("Invalid loop, one of the arguments didn't return a value", "", INVALID_INPUT_ERROR);
                    }
                }
            }
        }

        return Result.success(result);
    }

    @Override
    public String parser() {
        return null;
    }
}
