package net.minestom.server.command.builder.arguments.number;

import net.minestom.server.command.builder.arguments.Argument;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public abstract class ArgumentNumber<T extends Number> extends Argument<T> {

    public static final int NOT_NUMBER_ERROR = 1;
    public static final int RANGE_ERROR = 2;

    protected boolean hasMin, hasMax;
    protected T min, max;

    public ArgumentNumber(String id) {
        super(id, false);
    }

    public ArgumentNumber<T> min(T value) {
        this.min = value;
        this.hasMin = true;
        return this;
    }

    public ArgumentNumber<T> max(T value) {
        this.max = value;
        this.hasMax = true;

        return this;
    }

    public ArgumentNumber<T> between(T min, T max) {
        this.min = min;
        this.max = max;
        this.hasMin = true;
        this.hasMax = true;
        return this;
    }

    /**
     * Get if the argument has a minimum
     *
     * @return true if the argument has a minimum
     */
    public boolean hasMin() {
        return hasMin;
    }

    /**
     * Get the minimum value for this argument
     *
     * @return the minimum of this argument
     */
    public T getMin() {
        return min;
    }

    /**
     * Get if the argument has a maximum
     *
     * @return true if the argument has a maximum
     */
    public boolean hasMax() {
        return hasMax;
    }

    /**
     * Get the maximum value for this argument
     *
     * @return the maximum of this argument
     */
    public T getMax() {
        return max;
    }

    protected String parseValue(String value) {
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

    protected int getRadix(String value) {
        if (value.startsWith("0b")) {
            return 2;
        } else if (value.startsWith("0x")) {
            return 16;
        }
        return 10;
    }

    protected String removeScientificNotation(String value) {
        try {
            return new BigDecimal(value).toPlainString();
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
