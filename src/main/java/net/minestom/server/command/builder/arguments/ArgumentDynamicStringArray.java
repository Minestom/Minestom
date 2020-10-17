package net.minestom.server.command.builder.arguments;

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
