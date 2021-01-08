package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.minecraft.SuggestionType;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.utils.callback.validator.StringValidator;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Same as {@link ArgumentWord} with the exception
 * that this argument can trigger {@link net.minestom.server.command.builder.Command#onDynamicWrite(CommandSender, String)}
 * when the suggestion type is {@link SuggestionType#ASK_SERVER}, or any other suggestions available in the enum.
 */
public class ArgumentDynamicWord extends Argument<String> {

    public static final int SPACE_ERROR = 1;
    public static final int RESTRICTION_ERROR = 2;

    private final SuggestionType suggestionType;

    private StringValidator dynamicRestriction;

    public ArgumentDynamicWord(@NotNull String id, @NotNull SuggestionType suggestionType) {
        super(id);
        this.suggestionType = suggestionType;
    }

    @NotNull
    @Override
    public String parse(@NotNull String input) throws ArgumentSyntaxException {
        if (input.contains(StringUtils.SPACE))
            throw new ArgumentSyntaxException("Word cannot contain space characters", input, SPACE_ERROR);

        // true if 'value' is valid based on the dynamic restriction
        final boolean restrictionCheck = dynamicRestriction == null || dynamicRestriction.isValid(input);

        if (!restrictionCheck) {
            throw new ArgumentSyntaxException("Word does not respect the dynamic restriction", input, RESTRICTION_ERROR);
        }

        return input;
    }

    /**
     * Gets the suggestion type of this argument.
     * <p>
     * Suggestions are only suggestion, this means that the end value could not be the expected one.
     *
     * @return this argument suggestion type
     */
    @NotNull
    public SuggestionType getSuggestionType() {
        return suggestionType;
    }

    /**
     * Sets the dynamic restriction of this dynamic argument.
     * <p>
     * Will be called once the argument condition is checked.
     *
     * @param dynamicRestriction the dynamic restriction, can be null to disable
     * @return 'this' for chaining
     */
    public ArgumentDynamicWord fromRestrictions(@Nullable StringValidator dynamicRestriction) {
        this.dynamicRestriction = dynamicRestriction;
        return this;
    }
}
