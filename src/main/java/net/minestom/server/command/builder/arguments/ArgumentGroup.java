package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.CommandReader;
import net.minestom.server.command.builder.CommandContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ArgumentGroup extends Argument<CommandContext> {
    //todo don't return context

    public static final int INVALID_ARGUMENTS_ERROR = 1;

    private final Argument<?>[] group;

    public ArgumentGroup(@NotNull String id, @NotNull Argument<?>... group) {
        super(id);
        this.group = group;
    }


    @Override
    public @NotNull Result<CommandContext> parse(CommandReader reader) {
        final Map<String, Object> results = new HashMap<>();
        for (Argument<?> argument : group) {
            if (!reader.hasRemaining()) {
                final Supplier<?> supplier = argument.getDefaultValue();
                if (supplier != null) {
                    results.put(argument.getId(), supplier.get());
                } else {
                    return Result.syntaxError("Required arg isn't specified", "", INVALID_ARGUMENTS_ERROR);
                }
            }
            final Result<?> result = argument.parse(reader);
            if (result instanceof Result.Success<?> success) {
                results.put(argument.getId(), success.value());
            } else {
                return (Result<CommandContext>) result;
            }
        }
        return Result.success(new CommandContext("").setArgs(results));
    }

    @Override
    public String parser() {
        return null;
    }

    public List<Argument<?>> group() {
        return List.of(group);
    }
}
