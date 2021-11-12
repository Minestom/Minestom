package net.minestom.server.command.builder.exception;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.FixedStringReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A command exception that should be used when you need to throw an exception while parsing a section of an argument.
 */
public class SectionParsingException extends RenderedCommandException {

    /**
     * The error code to represent the translatable key "parsing.quote.expected.start".
     */
    public static final int QUOTE_EXPECTED_START = 1;

    /**
     * The error code to represent the translatable key "parsing.quote.expected.end".
     */
    public static final int QUOTE_EXPECTED_END = 2;

    /**
     * The error code to represent the translatable key "parsing.quote.escape".
     */
    public static final int QUOTE_ESCAPE = 3;

    /**
     * The error code to represent the translatable key "parsing.bool.invalid".
     */
    public static final int BOOL_INVALID = 4;

    /**
     * The error code to represent the translatable key "parsing.bool.expected".
     */
    public static final int BOOL_EXPECTED = 5;

    /**
     * The error code to represent the translatable key "parsing.int.invalid".
     */
    public static final int INT_INVALID = 6;

    /**
     * The error code to represent the translatable key "parsing.int.expected".
     */
    public static final int INT_EXPECTED = 7;

    /**
     * The error code to represent the translatable key "parsing.long.invalid".
     */
    public static final int LONG_INVALID = 8;

    /**
     * The error code to represent the translatable key "parsing.long.expected".
     */
    public static final int LONG_EXPECTED = 9;

    /**
     * The error code to represent the translatable key "parsing.double.invalid".
     */
    public static final int DOUBLE_INVALID = 10;

    /**
     * The error code to represent the translatable key "parsing.double.expected".
     */
    public static final int DOUBLE_EXPECTED = 11;

    /**
     * The error code to represent the translatable key "parsing.float.invalid".
     */
    public static final int FLOAT_INVALID = 12;

    /**
     * The error code to represent the translatable key "parsing.float.expected".
     */
    public static final int FLOAT_EXPECTED = 13;

    /**
     * The error code to represent the translatable key "parsing.expected".
     */
    public static final int EXPECTED = 14;

    /**
     * Creates a new SectionParsingException with the provided error code and reader. The error message and error
     * component are automatically generated via {@link #getDisplayMessage(int, String)} and
     * {@link #getDisplayComponent(int, String)}.
     */
    public SectionParsingException(int errorCode, @NotNull FixedStringReader reader){
        super(getDisplayMessage(errorCode, null), errorCode, reader, getDisplayComponent(errorCode, null));
    }

    /**
     * Creates a new SectionParsingException with the provided error code, reader, and value. The error message and
     * error component are automatically generated via {@link #getDisplayMessage(int, String)} and
     * {@link #getDisplayComponent(int, String)}.
     */
    public SectionParsingException(int errorCode, @NotNull FixedStringReader reader, @Nullable String value){
        super(getDisplayMessage(errorCode, value), errorCode, reader, getDisplayComponent(errorCode, value));
    }

    /**
     * Throws an IllegalArgumentException if {@code value} is null. Otherwise, returns {@code Component.text(value)}
     */
    private static @NotNull Component assureAndMake(@Nullable String value){
        if (value == null){
            throw new IllegalArgumentException("Expected a non-null value for parameter \"value\"!");
        }
        return Component.text(value);
    }

    /**
     * @return the error, as a component, that should be displayed for the provided error code and (optional based on
     * context) value
     */
    public static @NotNull Component getDisplayComponent(int errorCode, @Nullable String value){
        return switch(errorCode){
            case QUOTE_EXPECTED_START -> Component.translatable("parsing.quote.expected.start", FixedStringReader.RED_STYLE);
            case QUOTE_EXPECTED_END -> Component.translatable("argument.double.big", FixedStringReader.RED_STYLE);
            case QUOTE_ESCAPE -> Component.translatable("parsing.quote.escape", FixedStringReader.RED_STYLE, assureAndMake(value));
            case BOOL_INVALID -> Component.translatable("parsing.bool.invalid", FixedStringReader.RED_STYLE, assureAndMake(value));
            case BOOL_EXPECTED -> Component.translatable("parsing.bool.expected", FixedStringReader.RED_STYLE);
            case INT_INVALID -> Component.translatable("parsing.int.invalid", FixedStringReader.RED_STYLE, assureAndMake(value));
            case INT_EXPECTED -> Component.translatable("parsing.int.expected", FixedStringReader.RED_STYLE);
            case LONG_INVALID -> Component.translatable("parsing.long.invalid", FixedStringReader.RED_STYLE, assureAndMake(value));
            case LONG_EXPECTED -> Component.translatable("parsing.long.expected", FixedStringReader.RED_STYLE);
            case DOUBLE_INVALID -> Component.translatable("parsing.double.invalid", FixedStringReader.RED_STYLE, assureAndMake(value));
            case DOUBLE_EXPECTED -> Component.translatable("parsing.double.expected", FixedStringReader.RED_STYLE);
            case FLOAT_INVALID -> Component.translatable("parsing.float.invalid", FixedStringReader.RED_STYLE, assureAndMake(value));
            case FLOAT_EXPECTED -> Component.translatable("parsing.float.expected", FixedStringReader.RED_STYLE);
            case EXPECTED -> Component.translatable("parsing.expected", FixedStringReader.RED_STYLE, assureAndMake(value));
            default -> throw new IllegalArgumentException("Invalid error code " + errorCode + "!");
        };
    }

    /**
     * @return the error, as a string, that should be displayed for the provided error code and (optional based on
     * context) value
     */
    public static @NotNull String getDisplayMessage(int errorCode, @Nullable String value){
        return switch(errorCode){
            case QUOTE_EXPECTED_START -> "Expected quote to start a string";
            case QUOTE_EXPECTED_END -> "Unclosed quoted string";
            case QUOTE_ESCAPE -> "Invalid escape sequence '\\" + value + "' in quoted string";
            case BOOL_INVALID -> "Invalid boolean, expected 'true' or 'false' but found '" + value + "'";
            case BOOL_EXPECTED -> "Expected boolean";
            case INT_INVALID -> "Invalid integer '" + value + "'";
            case INT_EXPECTED -> "Expected integer";
            case LONG_INVALID -> "Invalid long '" + value + "'";
            case LONG_EXPECTED -> "Expected long";
            case DOUBLE_INVALID -> "Invalid double '" + value + "'";
            case DOUBLE_EXPECTED -> "Expected double";
            case FLOAT_INVALID -> "Invalid float '" + value + "'";
            case FLOAT_EXPECTED -> "Expected float";
            case EXPECTED -> "Expected '" + value + "'";
            default -> throw new IllegalArgumentException("Invalid error code " + errorCode + "!");
        };
    }
}
