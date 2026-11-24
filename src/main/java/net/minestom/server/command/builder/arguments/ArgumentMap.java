package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

/**
 * An argument whose parsed value is converted by a mapper.
 *
 * @param <I> the wrapped argument's result type
 * @param <O> the mapped result type
 */
@ApiStatus.Experimental
public final class ArgumentMap<I, O> extends Argument<O> {
    static final int INVALID_MAP = 555;
    private final Argument<I> argument;
    private final BiFunction<CommandSender, I, O> mapper;

    ArgumentMap(Argument<I> argument, BiFunction<CommandSender, I, O> mapper) {
        super(argument.getId(), argument.allowSpace(), argument.useRemaining());
        this.suggestionType = argument.suggestionType();
        var suggestionCallback = argument.getSuggestionCallback();
        if (suggestionCallback != null)
            this.setSuggestionCallback(suggestionCallback);
        var defaultValue = argument.getDefaultValue();
        if (defaultValue != null)
            this.setDefaultValue(sender -> mapper.apply(sender, defaultValue.apply(sender)));
        this.argument = argument;
        this.mapper = mapper;
    }

    /**
     * Returns the argument whose result is mapped.
     *
     * @return the wrapped argument
     */
    public Argument<I> argument() {
        return argument;
    }

    @Override
    public O parse(CommandSender sender, String input) throws ArgumentSyntaxException {
        final I value = argument.parse(sender, input);
        final O mappedValue = mapper.apply(sender, value);
        if (mappedValue == null)
            throw new ArgumentSyntaxException("Couldn't be converted to map type", input, INVALID_MAP);
        return mappedValue;
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
