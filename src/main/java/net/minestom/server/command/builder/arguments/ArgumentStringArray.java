package net.minestom.server.command.builder.arguments;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * Represents an argument which will take all the remaining of the command.
 * <p>
 * Example: Hey I am a string
 */
public class ArgumentStringArray extends Argument<String[]> {

    public ArgumentStringArray(String id) {
        super(id, true, true);
    }

    @Override
    public int getCorrectionResult(@NotNull String value) {
        return SUCCESS;
    }

    @NotNull
    @Override
    public String[] parse(@NotNull String value) {
        return value.split(Pattern.quote(" "));
    }

    @Override
    public int getConditionResult(@NotNull String[] value) {
        return SUCCESS;
    }
}
