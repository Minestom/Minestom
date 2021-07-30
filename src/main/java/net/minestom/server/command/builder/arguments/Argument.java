package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.builder.ArgumentCallback;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandExecutor;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.command.builder.suggestion.SuggestionCallback;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
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
 * Arguments are parsed using {@link #parse(String)}.
 *
 * @param <T> the type of this parsed argument
 */
public abstract class Argument<T> {

    private final String id;
    protected final boolean allowSpace;
    protected final boolean useRemaining;

    private ArgumentCallback callback;

    private Supplier<T> defaultValue;

    private SuggestionCallback suggestionCallback;

    /**
     * Creates a new argument.
     *
     * @param id           the id of the argument, used to retrieve the parsed value
     * @param allowSpace   true if the argument can/should have spaces in it
     * @param useRemaining true if the argument will always take the rest of the command arguments
     */
    public Argument(@NotNull String id, boolean allowSpace, boolean useRemaining) {
        this.id = id;
        this.allowSpace = allowSpace;
        this.useRemaining = useRemaining;
    }

    /**
     * Creates a new argument with {@code useRemaining} sets to false.
     *
     * @param id         the id of the argument, used to retrieve the parsed value
     * @param allowSpace true if the argument can/should have spaces in it
     */
    public Argument(@NotNull String id, boolean allowSpace) {
        this(id, allowSpace, false);
    }

    /**
     * Creates a new argument with {@code useRemaining} and {@code allowSpace} sets to false.
     *
     * @param id the id of the argument, used to retrieve the parsed value
     */
    public Argument(@NotNull String id) {
        this(id, false, false);
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
        return argument.parse(argument.getId());
    }

    /**
     * Parses the given input, and throw an {@link ArgumentSyntaxException}
     * if the input cannot be converted to {@code T}
     *
     * @param input the argument to parse
     * @return the parsed argument
     * @throws ArgumentSyntaxException if {@code value} is not valid
     */
    public abstract @NotNull T parse(@NotNull String input) throws ArgumentSyntaxException;

    /**
     * Turns the argument into a list of nodes for command dispatching. Make sure to set the Node's parser.
     *
     * @param nodeMaker  helper object used to create and modify nodes
     * @param executable true if this will be the last argument, false otherwise
     */
    public abstract void processNodes(@NotNull NodeMaker nodeMaker, boolean executable);

    /**
     * Builds an argument node.
     *
     * @param argument   the argument
     * @param executable true if this will be the last argument, false otherwise
     * @return the created {@link DeclareCommandsPacket.Node}
     */
    @NotNull
    protected static DeclareCommandsPacket.Node simpleArgumentNode(@NotNull Argument<?> argument,
                                                                   boolean executable, boolean redirect, boolean suggestion) {
        DeclareCommandsPacket.Node argumentNode = new DeclareCommandsPacket.Node();

        argumentNode.flags = DeclareCommandsPacket.getFlag(DeclareCommandsPacket.NodeType.ARGUMENT, executable, redirect, suggestion);
        argumentNode.name = argument.getId();

        return argumentNode;
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
     * Gets if the argument can contain space.
     *
     * @return true if the argument allows space, false otherwise
     */
    public boolean allowSpace() {
        return allowSpace;
    }

    /**
     * Gets if the argument always use all the remaining characters.
     * <p>
     * ex: /help I am a test - will always give you "I am a test"
     * if the first and single argument does use the remaining.
     *
     * @return true if the argument use all the remaining characters, false otherwise
     */
    public boolean useRemaining() {
        return useRemaining;
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
        return this;
    }

    /**
     * Check if the argument has a suggestion.
     *
     * @return If this argument has a suggestion.
     */
    public boolean hasSuggestion() {
        return suggestionCallback != null;
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
            super(argument.getId(), argument.allowSpace(), argument.useRemaining());
            if (argument.getSuggestionCallback() != null)
                this.setSuggestionCallback(argument.getSuggestionCallback());
            if (argument.getDefaultValue() != null)
                this.setDefaultValue(() -> mapper.apply(argument.getDefaultValue().get()));
            this.argument = argument;
            this.mapper = mapper;
        }

        @Override
        public @NotNull O parse(@NotNull String input) throws ArgumentSyntaxException {
            final I value = argument.parse(input);
            final O mappedValue = mapper.apply(value);
            if (mappedValue == null)
                throw new ArgumentSyntaxException("Couldn't be converted to map type", input, INVALID_MAP);
            return mappedValue;
        }

        @Override
        public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
            argument.processNodes(nodeMaker, executable);
        }
    }

    private static final class ArgumentFilter<T> extends Argument<T> {
        public static final int INVALID_FILTER = 556;
        final Argument<T> argument;
        final Predicate<T> predicate;

        private ArgumentFilter(@NotNull Argument<T> argument, @NotNull Predicate<T> predicate) {
            super(argument.getId(), argument.allowSpace(), argument.useRemaining());
            if (argument.getSuggestionCallback() != null)
                this.setSuggestionCallback(argument.getSuggestionCallback());
            if (argument.getDefaultValue() != null)
                this.setDefaultValue(argument.getDefaultValue());
            this.argument = argument;
            this.predicate = predicate;
        }

        @Override
        public @NotNull T parse(@NotNull String input) throws ArgumentSyntaxException {
            final T result = argument.parse(input);
            if (!predicate.test(result))
                throw new ArgumentSyntaxException("Predicate failed", input, INVALID_FILTER);
            return result;
        }

        @Override
        public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
            argument.processNodes(nodeMaker, executable);
        }
    }
}
