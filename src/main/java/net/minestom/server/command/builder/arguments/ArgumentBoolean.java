package net.minestom.server.command.builder.arguments;

public class ArgumentBoolean extends Argument<Boolean> {

    public static final int NOT_BOOLEAN_ERROR = 1;

    public ArgumentBoolean(String id) {
        super(id, false);
    }

    @Override
    public int getCorrectionResult(String value) {
        return (value.equalsIgnoreCase("true")
                || value.equalsIgnoreCase("false")) ? SUCCESS : NOT_BOOLEAN_ERROR;
    }

    @Override
    public Boolean parse(String value) {
        return Boolean.valueOf(value);
    }

    @Override
    public int getConditionResult(Boolean value) {
        return SUCCESS;
    }

}
