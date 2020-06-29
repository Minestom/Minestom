package net.minestom.server.chat;

public class TranslatableText {

    private String code;
    private String[] arguments;

    private TranslatableText(String code, String[] arguments) {
        this.code = code;
        this.arguments = arguments;
    }

    public static TranslatableText of(String code) {
        return new TranslatableText(code, null);
    }

    public static TranslatableText of(String code, String... arguments) {
        return new TranslatableText(code, arguments);
    }

    @Override
    public String toString() {
        final String prefix = "{@";
        final String suffix = "}";

        String content = code;

        if (arguments != null && arguments.length > 0) {
            for (String arg : arguments) {
                content += "," + arg;
            }
        }

        return prefix + content + suffix;
    }
}
