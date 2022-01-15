package net.minestom.server.command.builder;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandOrigin;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.CommandException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Map;
import java.util.Objects;

public class ParsedCommand {

    private Command command;
    private CommandSyntax syntax;
    private String message;
    private Map<String, Object> argumentMap;
    private int readerPosition;
    private int argumentNumber;
    private CommandException exception;
    private boolean success;
    private int startingPosition;

    public @NotNull Command getCommand() {
        return command;
    }

    public @Nullable CommandSyntax getSyntax() {
        return syntax;
    }

    public @NotNull String getMessage() {
        return message;
    }

    public @Nullable Map<String, Object> getArgumentMap() {
        return argumentMap;
    }

    public int getReaderPosition() {
        return readerPosition;
    }

    public int getArgumentNumber() {
        return argumentNumber;
    }

    public @UnknownNullability CommandException getException() {
        return exception;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getStartingPosition() {
        return startingPosition;
    }

    @Contract("_ -> this")
    public @NotNull ParsedCommand setCommand(@NotNull Command command) {
        this.command = command;
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ParsedCommand setSyntax(@Nullable CommandSyntax syntax) {
        this.syntax = syntax;
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ParsedCommand setMessage(@NotNull String message) {
        this.message = message;
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ParsedCommand setArgumentMap(@Nullable Map<String, Object> argumentMap) {
        this.argumentMap = argumentMap;
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ParsedCommand setReaderPosition(int readerPosition) {
        this.readerPosition = readerPosition;
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ParsedCommand setArgumentNumber(int argumentNumber) {
        this.argumentNumber = argumentNumber;
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ParsedCommand setException(@Nullable CommandException exception) {
        this.exception = exception;
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ParsedCommand setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    @Contract("_ -> this")
    public @NotNull ParsedCommand setStartingPosition(int startingPosition) {
        this.startingPosition = startingPosition;
        return this;
    }

    @Contract(" -> new")
    public @NotNull CommandContext toContext() {
        return new CommandContext(this.message, this.command, this.syntax, this.argumentMap, new CommandData(), this.exception, this.startingPosition);
    }

    public @Nullable CommandData execute(@NotNull CommandOrigin origin) {
        CommandContext context = toContext();

        command.globalListener(origin, context);

        if (command.getCondition() != null && !command.getCondition().canUse(origin, message, startingPosition)) {
            return null;
        }

        // If it's a failure, look for the correct argument. If there is none, just use the default executor. If there
        // is no default executor at this point, there is no other way to notify the command of this failed execution.
        if (!isSuccess()) {
            if (exception != null && syntax != null && argumentNumber >= 0 && argumentNumber < syntax.getArguments().size()) {
                Argument<?> exactArgument = syntax.getArguments().get(argumentNumber);
                if (exactArgument.getCallback() != null) {
                    try {
                        exactArgument.getCallback().apply(origin, exception);
                    } catch (Throwable throwable) {
                        MinecraftServer.getExceptionManager().handleException(throwable);
                    }
                    return context.getData();
                }
            }
            if (command.getDefaultExecutor() != null) {
                try {
                    command.getDefaultExecutor().apply(origin, context);
                } catch (Throwable throwable) {
                    MinecraftServer.getExceptionManager().handleException(throwable);
                }
            }
            return context.getData();
        }

        if (syntax == null) {
            if (command.getDefaultExecutor() != null) {
                try {
                    command.getDefaultExecutor().apply(origin, context);
                } catch (Throwable throwable) {
                    MinecraftServer.getExceptionManager().handleException(throwable);
                }
            }
        } else {
            try {
                syntax.getExecutor().apply(origin, context);
            } catch (Throwable throwable) {
                MinecraftServer.getExceptionManager().handleException(throwable);
            }
        }

        return context.getData();
    }

    @Override
    public String toString() {
        return "ParsedCommand[" +
                "command=" + command +
                ", syntax=" + syntax +
                ", message='" + message + '\'' +
                ", argumentMap=" + argumentMap +
                ", readerPosition=" + readerPosition +
                ", argumentNumber=" + argumentNumber +
                ", exception=" + exception +
                ", success=" + success +
                "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParsedCommand that = (ParsedCommand) o;
        return readerPosition == that.readerPosition && argumentNumber == that.argumentNumber &&
                success == that.success && Objects.equals(command, that.command) &&
                Objects.equals(syntax, that.syntax) && Objects.equals(message, that.message) &&
                Objects.equals(argumentMap, that.argumentMap) && Objects.equals(exception, that.exception);
    }

    @Override
    public int hashCode() {
        return Objects.hash(command, syntax, message, argumentMap, readerPosition, argumentNumber, exception, success);
    }
}
