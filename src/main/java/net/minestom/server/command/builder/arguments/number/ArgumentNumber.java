package net.minestom.server.command.builder.arguments.number;

import net.minestom.server.command.builder.arguments.Argument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public abstract class ArgumentNumber<T extends Number> extends Argument<T> {

    public static final int NOT_NUMBER_ERROR = 1;
    public static final int RANGE_ERROR = 2;

    protected boolean hasMin, hasMax;
    protected T min, max;

    public ArgumentNumber(String id) {
        super(id);
    }

    @NotNull
    public ArgumentNumber<T> min(@NotNull T value) {
        this.min = value;
        this.hasMin = true;
        return this;
    }

    @NotNull
    public ArgumentNumber<T> max(@NotNull T value) {
        this.max = value;
        this.hasMax = true;

        return this;
    }

    @NotNull
    public ArgumentNumber<T> between(@NotNull T min, @NotNull T max) {
        this.min = min;
        this.max = max;
        this.hasMin = true;
        this.hasMax = true;
        return this;
    }

    /**
     * Creates the byteflag based on the number's min/max existance.
     *
     * @return A byteflag for argument specification.
     */
    public byte getNumberProperties() {
        byte result = 0;
        if (this.hasMin())
            result |= 0x1;
        if (this.hasMax())
            result |= 0x2;
        return result;
    }

    /**
     * Gets if the argument has a minimum.
     *
     * @return true if the argument has a minimum
     */
    public boolean hasMin() {
        return hasMin;
    }

    /**
     * Gets the minimum value for this argument.
     *
     * @return the minimum of this argument
     */
    @NotNull
    public T getMin() {
        return min;
    }

    /**
     * Gets if the argument has a maximum.
     *
     * @return true if the argument has a maximum
     */
    public boolean hasMax() {
        return hasMax;
    }

    /**
     * Gets the maximum value for this argument.
     *
     * @return the maximum of this argument
     */
    @NotNull
    public T getMax() {
        return max;
    }

    @NotNull
    protected String parseValue(@NotNull String value) {
        if (value.startsWith("0b")) {
            value = value.replaceFirst(Pattern.quote("0b"), "");
        } else if (value.startsWith("0x")) {
            value = value.replaceFirst(Pattern.quote("0x"), "");
        } else if (value.toLowerCase().contains("e")) {
            value = removeScientificNotation(value);
        }
        // TODO number suffix support (k,m,b,t)
        return value;
    }

    protected int getRadix(@NotNull String value) {
        if (value.startsWith("0b")) {
            return 2;
        } else if (value.startsWith("0x")) {
            return 16;
        }
        return 10;
    }

    @Nullable
    protected String removeScientificNotation(@NotNull String value) {
        try {
            return new BigDecimal(value).toPlainString();
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
