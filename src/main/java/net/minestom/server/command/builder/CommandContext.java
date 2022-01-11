package net.minestom.server.command.builder;

import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.CommandException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    private final String message;
    private final Command command;
    private final CommandSyntax syntax;
    private Map<String, Object> argumentMap;
    private CommandData data;
    private CommandException exception;
    private int startingPosition;

    public CommandContext(@NotNull String message, @NotNull Command command, @Nullable CommandSyntax syntax,
                          @Nullable Map<String, Object> argumentMap, @Nullable CommandData data,
                          @Nullable CommandException exception, int startingPosition) {
        this.message = message;
        this.command = command;
        this.syntax = syntax;
        this.argumentMap = argumentMap == null ? new ConcurrentHashMap<>() : new ConcurrentHashMap<>(argumentMap);
        this.data = data == null ? new CommandData() : data;
        this.exception = exception;
        this.startingPosition = startingPosition;
    }

    public @NotNull String getMessage() {
        return message;
    }

    public @NotNull Command getCommand() {
        return command;
    }

    public @Nullable CommandSyntax getSyntax() {
        return syntax;
    }

    public @NotNull CommandData getData() {
        return data;
    }

    public @NotNull Map<String, Object> getArgumentMap() {
        return argumentMap;
    }

    public @Nullable CommandException getException() {
        return exception;
    }

    public int getStartingPosition() {
        return startingPosition;
    }

    public void setArgumentMap(@NotNull Map<String, Object> argumentMap) {
        this.argumentMap = argumentMap;
    }

    public void setException(@Nullable CommandException exception) {
        this.exception = exception;
    }

    public void setData(@NotNull CommandData data) {
        this.data = data;
    }

    public void setStartingPosition(int startingPosition) {
        this.startingPosition = startingPosition;
    }

    public <T> T get(@NotNull Argument<T> argument) {
        return get(argument.getId());
    }

    public <T> T get(@NotNull String id) {
        //noinspection unchecked
        return (T) argumentMap.get(id);
    }

    public <T> T getOrDefault(@NotNull Argument<T> argument, T defaultValue) {
        return getOrDefault(argument.getId(), defaultValue);
    }

    public <T> T getOrDefault(@NotNull String identifier, T defaultValue) {
        T value = get(identifier);
        return value == null ? defaultValue : value;
    }

    public boolean has(@NotNull Argument<?> argument) {
        return has(argument.getId());
    }

    public boolean has(@NotNull String id) {
        return argumentMap.containsKey(id);
    }

    public <T> void set(@NotNull Argument<T> argument, T value) {
        set(argument.getId(), value);
    }

    public <T> void set(@NotNull String id, T value) {
        this.argumentMap.put(id, value);
    }

}
