package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.CommandSender;
import net.minestom.server.utils.callback.validator.StringArrayValidator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * Same as {@link ArgumentStringArray} with the exception
 * that this argument can trigger {@link net.minestom.server.command.builder.Command#onDynamicWrite(CommandSender, String)}.
 */
public class ArgumentDynamicStringArray extends Argument<String[]> {

    public static final int RESTRICTION_ERROR = 1;

    private StringArrayValidator dynamicRestriction;

    public ArgumentDynamicStringArray(String id) {
        super(id, true, true);
    }

    @Override
    public int getCorrectionResult(@NotNull String value) {
        return SUCCESS;
    }

    @NotNull
    @Override
    public String[] parse(@NotNull String value) {
        return value.split(Pattern.quote(" "));
    }

    @Override
    public int getConditionResult(@NotNull String[] value) {

        // true if 'value' is valid based on the dynamic restriction
        final boolean restrictionCheck = dynamicRestriction == null || dynamicRestriction.isValid(value);

        if (!restrictionCheck) {
            return RESTRICTION_ERROR;
        }

        return SUCCESS;
    }

    /**
     * Sets the dynamic restriction of this dynamic argument.
     * <p>
     * Will be called once the argument condition is checked.
     *
     * @param dynamicRestriction the dynamic restriction, can be null to disable
     * @return 'this' for chaining
     */
    public ArgumentDynamicStringArray fromRestrictions(@Nullable StringArrayValidator dynamicRestriction) {
        this.dynamicRestriction = dynamicRestriction;
        return this;
    }

}
