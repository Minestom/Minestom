package net.minestom.server.command.builder.exception;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.FixedStringReader;
import org.jetbrains.annotations.NotNull;

/**
 * A command exception that should be used when a number argument is out of range.
 */
public class NumberRangeException extends RenderedCommandException {

    /**
     * The error code to represent the translatable key "argument.double.low"
     */
    public static final int DOUBLE_LOW = 1;

    /**
     * The error code to represent the translatable key "argument.double.big"
     */
    public static final int DOUBLE_BIG = 2;

    /**
     * The error code to represent the translatable key "argument.float.low"
     */
    public static final int FLOAT_LOW = 3;

    /**
     * The error code to represent the translatable key "argument.float.big"
     */
    public static final int FLOAT_BIG = 4;

    /**
     * The error code to represent the translatable key "argument.integer.low"
     */
    public static final int INTEGER_LOW = 5;

    /**
     * The error code to represent the translatable key "argument.integer.big"
     */
    public static final int INTEGER_BIG = 6;

    /**
     * The error code to represent the translatable key "argument.long.low"
     */
    public static final int LONG_LOW = 7;

    /**
     * The error code to represent the translatable key "argument.long.big"
     */
    public static final int LONG_BIG = 8;

    /**
     * Creates a new NumberRangeException with the provided error code, reader, value, and expected value. The error
     * message and error component are automatically generated via {@link #getDisplayMessage(int, String, String)} and
     * {@link #getDisplayComponent(int, String, String)}.
     */
    public NumberRangeException(int errorCode, @NotNull FixedStringReader reader, @NotNull String value, @NotNull String expected){
        super(getDisplayMessage(errorCode, value, expected), errorCode, reader, getDisplayComponent(errorCode, value, expected));
    }

    /**
     * @return the error, as a component, that should be displayed for the provided error code, value, and expected value
     */
    public static @NotNull Component getDisplayComponent(int errorCode, @NotNull String value, @NotNull String expected){
        return switch(errorCode){
            case DOUBLE_LOW -> Component.translatable("argument.double.low", FixedStringReader.RED_STYLE, Component.text(value), Component.text(expected));
            case DOUBLE_BIG -> Component.translatable("argument.double.big", FixedStringReader.RED_STYLE, Component.text(value), Component.text(expected));
            case FLOAT_LOW -> Component.translatable("argument.float.low", FixedStringReader.RED_STYLE, Component.text(value), Component.text(expected));
            case FLOAT_BIG -> Component.translatable("argument.float.big", FixedStringReader.RED_STYLE, Component.text(value), Component.text(expected));
            case INTEGER_LOW -> Component.translatable("argument.integer.low", FixedStringReader.RED_STYLE, Component.text(value), Component.text(expected));
            case INTEGER_BIG -> Component.translatable("argument.integer.big", FixedStringReader.RED_STYLE, Component.text(value), Component.text(expected));
            case LONG_LOW -> Component.translatable("argument.long.low", FixedStringReader.RED_STYLE, Component.text(value), Component.text(expected));
            case LONG_BIG -> Component.translatable("argument.long.big", FixedStringReader.RED_STYLE, Component.text(value), Component.text(expected));
            default -> throw new IllegalArgumentException("Invalid error code " + errorCode + "!");
        };
    }

    /**
     * @return the error, as a string, that should be displayed for the provided error code, value, and expected value
     */
    public static @NotNull String getDisplayMessage(int errorCode, @NotNull String value, @NotNull String expected){
        return switch(errorCode){
            case DOUBLE_LOW -> "Double must not be less than " + expected + ", found " + value;
            case DOUBLE_BIG -> "Double must not be more than " + expected + ", found " + value;
            case FLOAT_LOW -> "Float must not be less than " + expected + ", found " + value;
            case FLOAT_BIG -> "Float must not be more than " + expected + ", found " + value;
            case INTEGER_LOW -> "Integer must not be less than " + expected + ", found " + value;
            case INTEGER_BIG -> "Integer must not be more than " + expected + ", found " + value;
            case LONG_LOW -> "Long must not be less than " + expected + ", found " + value;
            case LONG_BIG -> "Long must not be more than " + expected + ", found " + value;
            default -> throw new IllegalArgumentException("Invalid error code " + errorCode + "!");
        };
    }
}
