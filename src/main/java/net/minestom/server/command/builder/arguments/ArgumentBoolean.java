package net.minestom.server.command.builder.arguments;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a boolean value.
 * <p>
 * Example: true
 */
public class ArgumentBoolean extends Argument<Boolean> {

    public static final int NOT_BOOLEAN_ERROR = 1;

    public ArgumentBoolean(String id) {
        super(id, false);
    }

    @Override
    public int getCorrectionResult(@NotNull String value) {
        return (value.equalsIgnoreCase("true")
                || value.equalsIgnoreCase("false")) ? SUCCESS : NOT_BOOLEAN_ERROR;
    }

    @NotNull
    @Override
    public Boolean parse(@NotNull String value) {
        return Boolean.parseBoolean(value);
    }

    @Override
    public int getConditionResult(@NotNull Boolean value) {
        return SUCCESS;
    }

}
