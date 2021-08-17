package net.minestom.server.command.builder.exception;

import net.minestom.server.command.builder.ArgumentCallback;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import org.jetbrains.annotations.NotNull;

/**
 * Exception triggered when an {@link Argument} is wrongly parsed.
 * <p>
 * Retrieved in {@link ArgumentCallback} defined in {@link Command#setArgumentCallback(ArgumentCallback, Argument)}.
 * <p>
 * Be aware that the message returned by {@link #getMessage()} is only here for debugging purpose,
 * you should refer to {@link #getErrorCode()} to identify the exceptions.
 */
public class ArgumentSyntaxException extends RuntimeException {

    private final String input;
    private final int errorCode;

    public ArgumentSyntaxException(@NotNull String message, @NotNull String input, int errorCode) {
        super(message);
        this.input = input;
        this.errorCode = errorCode;
    }

    /**
     * Gets the problematic command input.
     *
     * @return the command input which triggered the exception
     */
    @NotNull
    public String getInput() {
        return input;
    }

    /**
     * Gets the error code of the exception.
     * <p>
     * The code is decided arbitrary by the argument,
     * check the argument class to know the meaning of each one.
     *
     * @return the argument error code
     */
    public int getErrorCode() {
        return errorCode;
    }
}
