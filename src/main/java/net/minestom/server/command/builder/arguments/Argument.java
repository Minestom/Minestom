package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.CommandReader;
import net.minestom.server.command.builder.ArgumentCallback;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandExecutor;
import net.minestom.server.command.builder.arguments.minecraft.SuggestionType;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.command.builder.suggestion.SuggestionCallback;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * An argument is meant to be parsed when added into a {@link Command}'s syntax with {@link Command#addSyntax(CommandExecutor, Argument[])}.
 * <p>
 * You can create your own with your own special conditions.
 * <p>
 * Arguments are parsed using {@link #parse(CommandReader)}.
 *
 * @param <T> the type of this parsed argument
 */
public abstract class Argument<T> {

    private final String id;

    private ArgumentCallback callback;

    private Supplier<T> defaultValue;

    private SuggestionCallback suggestionCallback;
    protected SuggestionType suggestionType;

    /**
     * Creates a new argument.
     *
     * @param id the id of the argument, used to retrieve the parsed value
     */
    public Argument(@NotNull String id) {
        this.id = id;
    }

    /**
     * Parses an argument, using {@link Argument#getId()} as the input
     *
     * @param argument the argument, with the input as id
     * @param <T>      the result type
     * @return the parsed result
     * @throws ArgumentSyntaxException if the argument cannot be parsed due to a fault input (argument id)
     */
    @ApiStatus.Experimental
    public static <T> @NotNull T parse(@NotNull Argument<T> argument) throws ArgumentSyntaxException {
        return argument.parse(new CommandReader(argument.getId()));
    }

    /**
     * Tries to read the value from {@code reader}, if the next readable arg
     * is incompatible with this type (e.g. for an int arg the next word is a string)
     * it should throw {@link ArgumentSyntaxException} without moving the reader cursor
     * indicating that the type is incompatible, however if this method threw the exception
     * and also moved the cursor then the type considered compatible and parsing will stop
     * here resulting in a syntax error which on execution will call this argument's
     * {@link #getCallback()} if there is one.
     *
     * @param reader the command
     * @return the parsed argument
     * @throws ArgumentSyntaxException if {@code value} is not valid
     */
    public abstract @NotNull T parse(CommandReader reader) throws ArgumentSyntaxException;

    public abstract String parser();

    public byte @Nullable [] nodeProperties() {
        return null;
    }

    public @Nullable SuggestionType suggestionType() {
        return suggestionType;
    }

    /**
     * Gets the ID of the argument, showed in-game above the chat bar
     * and used to retrieve the data when the command is parsed in {@link net.minestom.server.command.builder.CommandContext}.
     *
     * @return the argument id
     */
    @NotNull
    public String getId() {
        return id;
    }

    /**
     * Gets the {@link ArgumentCallback} to check if the argument-specific conditions are validated or not.
     *
     * @return the argument callback, null if not any
     */
    @Nullable
    public ArgumentCallback getCallback() {
        return callback;
    }

    /**
     * Sets the {@link ArgumentCallback}.
     *
     * @param callback the argument callback, null to do not have one
     */
    public void setCallback(@Nullable ArgumentCallback callback) {
        this.callback = callback;
    }

    /**
     * Gets if the argument has any error callback.
     *
     * @return true if the argument has an error callback, false otherwise
     */
    public boolean hasErrorCallback() {
        return callback != null;
    }

    /**
     * Gets if this argument is 'optional'.
     * <p>
     * Optional means that this argument can be put at the end of a syntax
     * and obtains a default value ({@link #getDefaultValue()}).
     *
     * @return true if this argument is considered optional
     */
    public boolean isOptional() {
        return defaultValue != null;
    }

    @Nullable
    public Supplier<T> getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the default value supplier of the argument.
     * <p>
     * A non-null value means that the argument can be put at the end of a syntax
     * to act as an optional one.
     *
     * @param defaultValue the default argument value, null to make the argument non-optional
     * @return 'this' for chaining
     */
    @NotNull
    public Argument<T> setDefaultValue(@Nullable Supplier<T> defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    /**
     * Sets the default value supplier of the argument.
     *
     * @param defaultValue the default argument value
     * @return 'this' for chaining
     */
    @NotNull
    public Argument<T> setDefaultValue(@NotNull T defaultValue) {
        this.defaultValue = () -> defaultValue;
        return this;
    }

    /**
     * Gets the suggestion callback of the argument
     *
     * @return the suggestion callback of the argument, null if it doesn't exist
     * @see #setSuggestionCallback
     */
    @Nullable
    public SuggestionCallback getSuggestionCallback() {
        return suggestionCallback;
    }

    /**
     * Sets the suggestion callback (for dynamic tab completion) of this argument.
     * <p>
     * Note: This will not automatically filter arguments by user input.
     *
     * @param suggestionCallback The suggestion callback to set.
     * @return 'this' for chaining
     */
    public Argument<T> setSuggestionCallback(@NotNull SuggestionCallback suggestionCallback) {
        this.suggestionCallback = suggestionCallback;
        this.suggestionType = SuggestionType.ASK_SERVER;
        return this;
    }

    /**
     * Check if the argument has a suggestion.
     *
     * @return If this argument has a suggestion.
     */
    public boolean hasSuggestion() {
        return suggestionType != null;
    }

    /**
     * Maps this argument's output to another result.
     *
     * @param mapper The mapper to use (this argument's input = desired output)
     * @param <O>    The type of output expected.
     * @return A new ArgumentMap that can get this complex object type.
     */
    @ApiStatus.Experimental
    public <O> @NotNull Argument<O> map(@NotNull Function<T, O> mapper) {
        return new ArgumentMap<>(this, mapper);
    }

    /**
     * Maps this argument's output to another result.
     *
     * @param predicate the argument predicate
     * @return A new ArgumentMap that filters using this filterer.
     */
    @ApiStatus.Experimental
    public @NotNull Argument<T> filter(@NotNull Predicate<T> predicate) {
        return new ArgumentFilter<>(this, predicate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Argument<?> argument = (Argument<?>) o;

        return id.equals(argument.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    private static final class ArgumentMap<I, O> extends Argument<O> {
        public static final int INVALID_MAP = 555;
        final Argument<I> argument;
        final Function<I, O> mapper;

        private ArgumentMap(@NotNull Argument<I> argument, @NotNull Function<I, O> mapper) {
            super(argument.getId());
            if (argument.getSuggestionCallback() != null)
                this.setSuggestionCallback(argument.getSuggestionCallback());
            if (argument.getDefaultValue() != null)
                this.setDefaultValue(() -> mapper.apply(argument.getDefaultValue().get()));
            this.argument = argument;
            this.mapper = mapper;
        }

        @Override
        public @NotNull O parse(CommandReader reader) throws ArgumentSyntaxException {
            final I value = argument.parse(reader);
            final O mappedValue = mapper.apply(value);
            if (mappedValue == null)
                throw new ArgumentSyntaxException("Couldn't be converted to map type", value.toString(), INVALID_MAP);
            return mappedValue;
        }

        @Override
        public String parser() {
            return argument.parser();
        }

        @Override
        public byte @Nullable [] nodeProperties() {
            return argument.nodeProperties();
        }
    }

    private static final class ArgumentFilter<T> extends Argument<T> {
        public static final int INVALID_FILTER = 556;
        final Argument<T> argument;
        final Predicate<T> predicate;

        private ArgumentFilter(@NotNull Argument<T> argument, @NotNull Predicate<T> predicate) {
            super(argument.getId());
            if (argument.getSuggestionCallback() != null)
                this.setSuggestionCallback(argument.getSuggestionCallback());
            if (argument.getDefaultValue() != null)
                this.setDefaultValue(argument.getDefaultValue());
            this.argument = argument;
            this.predicate = predicate;
        }

        @Override
        public @NotNull T parse(CommandReader reader) throws ArgumentSyntaxException {
            final T result = argument.parse(reader);
            if (!predicate.test(result))
                throw new ArgumentSyntaxException("Predicate failed", result.toString(), INVALID_FILTER);
            return result;
        }

        @Override
        public String parser() {
            return argument.parser();
        }

        @Override
        public byte @Nullable [] nodeProperties() {
            return argument.nodeProperties();
        }
    }
}
