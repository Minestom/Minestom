package net.minestom.server.command.builder.arguments;

/**
 * Same as {@link ArgumentWord} with the exception
 * that this argument can trigger {@link net.minestom.server.command.builder.Command#onDynamicWrite(String)}.
 */
public class ArgumentDynamicWord extends Argument<String> {

    public ArgumentDynamicWord(String id) {
        super(id);
    }

    @Override
    public int getCorrectionResult(String value) {
        return SUCCESS;
    }

    @Override
    public String parse(String value) {
        return value;
    }

    @Override
    public int getConditionResult(String value) {
        return SUCCESS;
    }
}
