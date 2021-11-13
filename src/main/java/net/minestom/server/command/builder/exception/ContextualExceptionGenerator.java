package net.minestom.server.command.builder.exception;

import org.jetbrains.annotations.NotNull;

/**
 * A class that represents the default ContextualExceptionGenerator
 */
public abstract class ContextualExceptionGenerator {

    private final String translationKey;
    private final int errorCode;

    /**
     * Creates a new ContextualExceptionGenerator with the provided translation key and error code
     */
    public ContextualExceptionGenerator(@NotNull String translationKey, int errorCode){
        this.translationKey = translationKey;
        this.errorCode = errorCode;
    }

    /**
     * @return the translation key that should be used in other methods of this generator
     */
    public @NotNull String translationKey(){
        return translationKey;
    }

    /**
     * @return the error code that should be used when generating exceptions
     */
    public int errorCode() {
        return errorCode;
    }

    /**
     * Assures that the provided string has {@code expected} placeholders. If it does not, this method throws an exception.
     * Otherwise, it returns the string split into its parts.
     */
    protected @NotNull String @NotNull [] assurePlaceholders(@NotNull String exceptionMessage, int expected){
        String[] split = exceptionMessage.split("%s");
        if ((split.length - 1) != expected){
            StringBuilder builder = new StringBuilder("Expected the exception message to have ").append(expected).append(" placeholder");
            if (expected != 1){
                builder.append("s");
            }
            throw new IllegalArgumentException(builder.append(", found ").append(split.length - 1).append(".").toString());
        }
        return split;
    }
}
