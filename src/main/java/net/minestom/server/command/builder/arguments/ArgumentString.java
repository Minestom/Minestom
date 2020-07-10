package net.minestom.server.command.builder.arguments;

public class ArgumentString extends Argument<String> {

    public static final int QUOTE_ERROR = 1;

    public ArgumentString(String id) {
        super(id, true);
    }

    @Override
    public int getCorrectionResult(String value) {
        // Check if value start and end with quote
        char first = value.charAt(0);
        char last = value.charAt(value.length() - 1);
        boolean quote = first == '\"' && last == '\"';
        if (!quote)
            return QUOTE_ERROR;

        for (int i = 1; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\"') {
                char lastChar = value.charAt(i - 1);
                if (lastChar == '\\') {
                    continue;
                } else if (i == value.length() - 1) {
                    return SUCCESS;
                }
            }
        }

        // Last quote is written like \"
        return QUOTE_ERROR;
    }

    @Override
    public String parse(String value) {
        // Remove first and last characters (quote)
        value = value.substring(1, value.length() - 1);

        // Remove all backslashes
        value = value.replace("\\", "");

        return value;
    }

    @Override
    public int getConditionResult(String value) {
        return SUCCESS;
    }
}
