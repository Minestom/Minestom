package net.minestom.server.command.builder.arguments;

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
    public int getCorrectionResult(String value) {
        return SUCCESS;
    }

    @Override
    public String[] parse(String value) {
        return value.split(Pattern.quote(" "));
    }

    @Override
    public int getConditionResult(String[] value) {
        return SUCCESS;
    }
}
