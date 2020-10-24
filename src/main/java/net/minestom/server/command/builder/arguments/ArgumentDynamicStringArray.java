package net.minestom.server.command.builder.arguments;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * Same as {@link ArgumentStringArray} with the exception
 * that this argument can trigger {@link net.minestom.server.command.builder.Command#onDynamicWrite(String)}.
 */
public class ArgumentDynamicStringArray extends Argument<String[]> {

    public ArgumentDynamicStringArray(String id) {
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
