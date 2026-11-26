package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * An argument whose parsed value must satisfy a predicate.
 *
 * @param <T> the wrapped argument's result type
 */
@ApiStatus.Experimental
public final class ArgumentFilter<T> extends Argument<T> {
    static final int INVALID_FILTER = 556;
    private final Argument<T> argument;
    private final Predicate<T> predicate;

    ArgumentFilter(Argument<T> argument, Predicate<T> predicate) {
        super(argument.getId(), argument.allowSpace(), argument.useRemaining());
        this.suggestionType = argument.suggestionType();
        var suggestionCallback = argument.getSuggestionCallback();
        if (suggestionCallback != null)
            this.setSuggestionCallback(suggestionCallback);
        var defaultValue = argument.getDefaultValue();
        if (defaultValue != null)
            this.setDefaultValue(defaultValue);
        this.argument = argument;
        this.predicate = predicate;
    }

    /**
     * Returns the argument whose result is filtered.
     *
     * @return the wrapped argument
     */
    public Argument<T> argument() {
        return argument;
    }

    @Override
    public T parse(CommandSender sender, String input) throws ArgumentSyntaxException {
        final T result = argument.parse(sender, input);
        if (!predicate.test(result))
            throw new ArgumentSyntaxException("Predicate failed", input, INVALID_FILTER);
        return result;
    }

    @Override
    public ArgumentParserType parser() {
        return argument.parser();
    }

    @Override
    public byte @Nullable [] nodeProperties() {
        return argument.nodeProperties();
    }
}
