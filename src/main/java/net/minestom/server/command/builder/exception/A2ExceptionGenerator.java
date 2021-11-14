package net.minestom.server.command.builder.exception;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.FixedStringReader;
import org.jetbrains.annotations.NotNull;

/**
 * An exception generator that has two placeholders.
 */
public class A2ExceptionGenerator extends ContextualExceptionGenerator {

    private final String[] exceptionMessage;

    /**
     * Creates a new contextual exception generator that requires 2 placeholders to generate the message. For
     * placeholders, use {@link CommandException#PLACEHOLDER}.
     */
    public A2ExceptionGenerator(@NotNull String translationKey, int errorCode, @NotNull String exceptionMessage){
        super(translationKey, errorCode);
        this.exceptionMessage = assurePlaceholders(exceptionMessage, 2);
    }

    /**
     * @return the component that can be used to represent this instance
     */
    public @NotNull Component generateComponent(@NotNull String arg1, @NotNull String arg2){
        return Component.translatable(translationKey(), FixedStringReader.RED_STYLE, Component.text(arg1), Component.text(arg2));
    }

    /**
     * @return the exception message for this instance based on the provided arguments
     */
    public @NotNull String generateExceptionMessage(@NotNull String arg1, @NotNull String arg2){
        return exceptionMessage[0] + arg1 + exceptionMessage[1] + arg2 + exceptionMessage[2];
    }

    /**
     * @return a new CommandException based on this instance, the provided string reader, and the provided
     * placeholders.
     */
    public @NotNull CommandException generateException(@NotNull FixedStringReader reader, @NotNull String arg1, @NotNull String arg2){
        return new CommandException(generateExceptionMessage(arg1, arg2), errorCode(), reader, generateComponent(arg1, arg2));
    }

}