package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.CommandReader;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;

@SuppressWarnings("rawtypes")
public class ArgumentEnum<E extends Enum> extends Argument<E> {
    private final E[] values;
    private Format format = Format.DEFAULT;

    public ArgumentEnum(@NotNull String id, Class<E> enumClass) {
        super(id);
        this.values = enumClass.getEnumConstants();
    }

    public ArgumentEnum<E> setFormat(@NotNull Format format) {
        this.format = format;
        return this;
    }

    @Override
    public @NotNull Result<E> parse(CommandReader reader) {
        final String input = reader.readWord();
        for (E value : this.values) {
            if (this.format.formatter.apply(value.name()).equals(input)) {
                return Result.success(value);
            }
        }
        return Result.incompatibleType();
    }

    @Override
    public String parser() {
        return null;
    }

    public List<String> entries() {
        return Arrays.stream(values).map(x -> format.formatter.apply(x.name())).toList();
    }

    public enum Format {
        DEFAULT(name -> name),
        LOWER_CASED(name -> name.toLowerCase(Locale.ROOT)),
        UPPER_CASED(name -> name.toUpperCase(Locale.ROOT));

        private final UnaryOperator<String> formatter;

        Format(@NotNull UnaryOperator<String> formatter) {
            this.formatter = formatter;
        }
    }

    @Override
    public String toString() {
        return String.format("Enum<%s>", getId());
    }
}
