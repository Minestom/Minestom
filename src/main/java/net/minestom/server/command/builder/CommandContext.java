package net.minestom.server.command.builder;

import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Class used to retrieve argument data in a {@link CommandExecutor}.
 * <p>
 * All id are the one specified in the {@link Argument} constructor.
 * <p>
 * All methods are @{@link NotNull} in the sense that you should not have to verify their validity since if the syntax
 * is called, it means that all of its arguments are correct. Be aware that trying to retrieve an argument not present
 * in the syntax will result in a {@link NullPointerException}.
 */
public class CommandContext {

    private final String input;
    private final String commandName;
    protected Map<String, Object> args = new HashMap<>();
    protected Map<String, String> rawArgs = new HashMap<>();
    private CommandData returnData;

    public CommandContext(@NotNull String input) {
        this.input = input;
        this.commandName = input.split(StringUtils.SPACE)[0];
    }

    public @NotNull String getInput() {
        return input;
    }

    public @NotNull String getCommandName() {
        return commandName;
    }

    public <T> T get(@NotNull Argument<T> argument) {
        return get(argument.getId());
    }

    public <T> T get(@NotNull String identifier) {
        return (T) args.get(identifier);
    }

    public <T> T getOrDefault(@NotNull Argument<T> argument, T defaultValue) {
        return getOrDefault(argument.getId(), defaultValue);
    }

    public <T> T getOrDefault(@NotNull String identifier, T defaultValue) {
        T value;
        return (value = get(identifier)) != null ? value : defaultValue;
    }

    public boolean has(@NotNull Argument<?> argument) {
        return args.containsKey(argument.getId());
    }

    public boolean has(@NotNull String identifier) {
        return args.containsKey(identifier);
    }

    public @Nullable CommandData getReturnData() {
        return returnData;
    }

    public void setReturnData(@Nullable CommandData returnData) {
        this.returnData = returnData;
    }

    public @NotNull Map<String, Object> getMap() {
        return args;
    }

    public void copy(@NotNull CommandContext context) {
        this.args = context.args;
        this.rawArgs = context.rawArgs;
    }

    public String getRaw(@NotNull Argument<?> argument) {
        return rawArgs.get(argument.getId());
    }

    public String getRaw(@NotNull String identifier) {
        return rawArgs.computeIfAbsent(identifier, s -> {
            throw new NullPointerException(
                    "The argument with the id '" + identifier + "' has no value assigned, be sure to check your arguments id, your syntax, and that you do not change the argument id dynamically.");
        });
    }

    public void setArg(@NotNull String id, Object value, String rawInput) {
        this.args.put(id, value);
        this.rawArgs.put(id, rawInput);
    }

    protected void clear() {
        this.args.clear();
    }

    protected void retrieveDefaultValues(@Nullable Map<String, Supplier<Object>> defaultValuesMap) {
        if (defaultValuesMap == null) return;
        for (var entry : defaultValuesMap.entrySet()) {
            final String key = entry.getKey();
            if (!args.containsKey(key)) {
                final var supplier = entry.getValue();
                this.args.put(key, supplier.get());
            }
        }
    }
}
