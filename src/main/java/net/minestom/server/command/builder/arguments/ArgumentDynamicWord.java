package net.minestom.server.command.builder.arguments;

import org.jetbrains.annotations.NotNull;

/**
 * Same as {@link ArgumentWord} with the exception
 * that this argument can trigger {@link net.minestom.server.command.builder.Command#onDynamicWrite(String)}.
 */
public class ArgumentDynamicWord extends Argument<String> {

    public ArgumentDynamicWord(String id) {
        super(id);
    }

    @Override
    public int getCorrectionResult(@NotNull String value) {
        return SUCCESS;
    }

    @NotNull
    @Override
    public String parse(@NotNull String value) {
        return value;
    }

    @Override
    public int getConditionResult(@NotNull String value) {
        return SUCCESS;
    }
}
