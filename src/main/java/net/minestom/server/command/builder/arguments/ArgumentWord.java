package net.minestom.server.command.builder.arguments;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Represents a single word in the command.
 * <p>
 * You can specify the only correct words with {@link #from(String...)}.
 * <p>
 * Example: hey
 */
public class ArgumentWord extends Argument<String> {

    public static final int SPACE_ERROR = 1;
    public static final int RESTRICTION_ERROR = 2;

    private String[] restrictions;

    public ArgumentWord(String id) {
        super(id, false);
    }

    public ArgumentWord from(String... restrictions) {
        this.restrictions = restrictions;
        return this;
    }

    @Override
    public int getCorrectionResult(@NotNull String value) {
        if (value.contains(" "))
            return SPACE_ERROR;

        return SUCCESS;
    }

    @NotNull
    @Override
    public String parse(@NotNull String value) {
        return value;
    }

    @Override
    public int getConditionResult(@NotNull String value) {
        // Check restrictions
        if (restrictions != null && restrictions.length > 0) {
            for (String r : restrictions) {
                if (value.equalsIgnoreCase(r))
                    return SUCCESS;
            }
            return RESTRICTION_ERROR;
        }

        return SUCCESS;
    }

    public boolean hasRestrictions() {
        return restrictions != null && restrictions.length > 0;
    }

    public String[] getRestrictions() {
        return restrictions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArgumentWord that = (ArgumentWord) o;
        return Arrays.equals(restrictions, that.restrictions);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(restrictions);
    }
}
