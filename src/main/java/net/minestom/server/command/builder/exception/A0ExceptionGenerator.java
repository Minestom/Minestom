package net.minestom.server.command.builder.exception;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.FixedStringReader;
import org.jetbrains.annotations.NotNull;

/**
 * An exception generator that does not have any placeholders. Because of this, generated objects can be cached.
 */
public class A0ExceptionGenerator extends ContextualExceptionGenerator {

    private final String exceptionMessage;
    private final Component component;

    /**
     * Creates a new contextual exception generator that requires 0 placeholders to generate the message.
     */
    public A0ExceptionGenerator(@NotNull String translationKey, int errorCode, @NotNull String exceptionMessage){
        super(translationKey, errorCode);
        assurePlaceholders(exceptionMessage, 0);
        this.exceptionMessage = exceptionMessage;
        this.component = Component.translatable(translationKey, FixedStringReader.RED_STYLE);
    }

    /**
     * @return the component that can be used to represent this instance. The value from this method is cached internally.
     */
    public @NotNull Component generateComponent(){
        return component;
    }

    /**
     * @return the exception message for this instance
     */
    public @NotNull String generateExceptionMessage(){
        return exceptionMessage;
    }

    /**
     * @return a new RenderedCommandException based on this instance and the provided string reader
     */
    public @NotNull RenderedCommandException generateException(@NotNull FixedStringReader reader){
        return new RenderedCommandException(generateExceptionMessage(), errorCode(), reader, generateComponent());
    }

}
