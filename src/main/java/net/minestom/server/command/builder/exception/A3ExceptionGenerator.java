package net.minestom.server.command.builder.exception;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.FixedStringReader;
import org.jetbrains.annotations.NotNull;

/**
 * An exception generator that has three placeholders.
 */
public class A3ExceptionGenerator extends ContextualExceptionGenerator {

    private final String[] exceptionMessage;

    /**
     * Creates a new contextual exception generator that requires 3 placeholders to generate the message. For
     * placeholders, use {@link CommandException#PLACEHOLDER}.
     */
    public A3ExceptionGenerator(@NotNull String translationKey, int errorCode, @NotNull String exceptionMessage){
        super(translationKey, errorCode);
        this.exceptionMessage = assurePlaceholders(exceptionMessage, 3);
    }

    /**
     * @return the component that can be used to represent this instance
     */
    public @NotNull Component generateComponent(@NotNull String arg1, @NotNull String arg2, @NotNull String arg3){
        return Component.translatable(translationKey(), FixedStringReader.RED_STYLE, Component.text(arg1), Component.text(arg2), Component.text(arg3));
    }

    /**
     * @return the exception message for this instance based on the provided arguments
     */
    public @NotNull String generateExceptionMessage(@NotNull String arg1, @NotNull String arg2, @NotNull String arg3){
        return exceptionMessage[0] + arg1 + exceptionMessage[1] + arg2 + exceptionMessage[2] + arg3 + exceptionMessage[3];
    }

    /**
     * @return a new CommandException based on this instance, the provided text, the provided position, and the provided
     * placeholders.
     */
    public @NotNull CommandException generateException(@NotNull String text, int position, @NotNull String arg1, @NotNull String arg2, @NotNull String arg3){
        return new CommandException(text, position, errorCode(), generateComponent(arg1, arg2, arg2), generateExceptionMessage(arg1, arg2, arg3));
    }

}