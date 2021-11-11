package net.minestom.server.command.builder.exception;

import net.minestom.server.command.FixedStringReader;
import org.jetbrains.annotations.NotNull;

/**
 * This represents a basic command-related exception.
 */
public class CommandException extends RuntimeException {

    private final int errorCode;
    private final FixedStringReader stringReader;

    /**
     * Creates a new CommandException with the provided message, error code, and string reader.
     */
    public CommandException(@NotNull String message, int errorCode, @NotNull FixedStringReader stringReader){
        super(message);
        this.errorCode = errorCode;
        this.stringReader = stringReader;
    }

    /**
     * Creates a new CommandException with the provided error code and string reader.
     */
    public CommandException(int errorCode, @NotNull FixedStringReader stringReader){
        super();
        this.errorCode = errorCode;
        this.stringReader = stringReader;
    }

    /**
     * @return this exception's error code
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * @return this exception's string reader
     */
    public @NotNull FixedStringReader getStringReader() {
        return stringReader;
    }
}
