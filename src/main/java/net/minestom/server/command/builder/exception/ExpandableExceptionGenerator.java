package net.minestom.server.command.builder.exception;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.FixedStringReader;
import org.jetbrains.annotations.NotNull;

/**
 * An exception generator that adjusts the number of arguments it has based on the input. This class is slower to generate
 * components and messages (and it allocates more arrays) but it is infinitely expandable.
 */
public class ExpandableExceptionGenerator extends ContextualExceptionGenerator {

    private final String[] exceptionMessage;

    /**
     * Creates a new expandable exception generator with the provided translation key and the parts to the exception
     * message. This constructor turns the {@code exceptionMessage} parameter into its parts via splitting by
     * {@link CommandException#PATTERN_SAFE_PLACEHOLDER}.
     */
    public ExpandableExceptionGenerator(@NotNull String translationKey, int errorCode, @NotNull String exceptionMessage){
        super(translationKey, errorCode);
        this.exceptionMessage = exceptionMessage.split(CommandException.PATTERN_SAFE_PLACEHOLDER);
    }

    /**
     * @return the number of arguments that must be provided to this instance to generate information
     */
    public int argumentCount(){
        return exceptionMessage.length - 1;
    }

    /**
     * @return the component that can be used to represent this instance
     */
    public @NotNull Component generateComponent(@NotNull String @NotNull ... args){
        if (args.length != argumentCount()){
            throw new IllegalArgumentException("Expected " + argumentCount() + " arguments, found " + args.length);
        }
        Component[] components = new Component[args.length];
        for (int i = 0; i < args.length; i++){
            components[i] = Component.text(String.valueOf(args[i]));
        }
        return Component.translatable(translationKey(), FixedStringReader.RED_STYLE, components);
    }

    /**
     * @return the exception message for this instance based on the provided arguments
     */
    public @NotNull String generateExceptionMessage(@NotNull String @NotNull ... args){
        if (args.length != argumentCount()){
            throw new IllegalArgumentException("Expected " + argumentCount() + " arguments, found " + args.length);
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < argumentCount(); i++){
            builder.append(exceptionMessage[i]).append(args[i]);
        }
        return builder.append(exceptionMessage[exceptionMessage.length - 1]).toString();
    }

    /**
     * @return a new CommandException based on this instance, the provided string reader, and the provided
     * placeholders.
     */
    public @NotNull CommandException generateException(@NotNull FixedStringReader reader, @NotNull String @NotNull ... args){
        return new CommandException(generateExceptionMessage(args), errorCode(), reader, generateComponent(args));
    }
}