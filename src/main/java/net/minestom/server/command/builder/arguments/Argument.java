package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.StringReader;
import net.minestom.server.command.builder.ArgumentCallback;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandExecutor;
import net.minestom.server.command.builder.NodeMaker;
import net.minestom.server.command.builder.exception.CommandException;
import net.minestom.server.command.builder.suggestion.SuggestionCallback;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * An argument is meant to be parsed when added into a {@link Command}'s syntax with
 * {@link Command#addSyntax(CommandExecutor, Argument[])}.<br>
 * You can create your own with your own special conditions.<br>
 * Arguments are parsed using {@link #parse(StringReader)}.
 * @param <T> the type of this parsed argument
 */
public abstract class Argument<T> {

    private final String id;

    private ArgumentCallback callback;

    private Supplier<T> defaultValue;

    private SuggestionCallback suggestionCallback;

    private final boolean shouldCache;

    /**
     * Creates a new argument
     * @param id the id of the argument to use
     * @param shouldCache true if results containing this argument should be cached. This should be used, for example,
     *                    when arguments change how much they read based on outside factors
     */
    public Argument(@NotNull String id, boolean shouldCache) {
        this.id = id;
        this.shouldCache = shouldCache;
    }

    /**
     * Creates a new argument.
     *
     * @param id the id of the argument, used to retrieve the parsed value
     */
    public Argument(@NotNull String id) {
        this(id, true);
    }

    /**
     * Parses an argument, using {@link Argument#getId()} as the input
     *
     * @param argument the argument, with the input as id
     * @param <T>      the result type
     * @return the parsed result
     * @throws CommandException if the argument cannot be parsed due to a fault input (argument id)
     */
    @ApiStatus.Experimental
    public static <T> @NotNull T parse(@NotNull Argument<T> argument) throws CommandException {
        return argument.parse(new StringReader(argument.getId()));
    }

    /**
     * Reads from and parses the provided input, and throws a {@link CommandException} if there is an error
     * while reading the input into a {@code T}.<br>
     * <b>Note that this method is not abstract because the conversion to {@code StringReader}s is not complete.</b>
     */
    public abstract @NotNull T parse(@NotNull StringReader input) throws CommandException;

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
     * Gets the {@link ArgumentCallback} to check if the argument-specific conditions are validated or not.
     *
     * @return the argument callback, null if not any
     */
    @Nullable
    public ArgumentCallback getCallback() {
        return callback;
    }

    /**
     * Sets the {@link ArgumentCallback}.<br>
     * Returns itself for chaining.
     * @param callback the argument callback, null to not have one
     */
    @Contract("_ -> this")
    public @NotNull Argument<T> setCallback(@Nullable ArgumentCallback callback) {
        this.callback = callback;
        return this;
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
     * This boolean determines if commands that are executed that contain this should be cached. For example, if an
     * argument reads different amounts of text based on values that may change, it is a good idea to initialize it with
     * {@code shouldCache} as false. However, it's a bad idea to have arguments that behave like this.
     * @return true if results should be cached.
     */
    public boolean shouldCache() {
        return shouldCache;
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
        public @NotNull O parse(@NotNull StringReader input) throws CommandException {
            int pos = input.position();
            final I value = argument.parse(input);
            final O mappedValue = mapper.apply(value);
            if (mappedValue == null) {
                // TODO: Consider throwing a more accurate exception
                throw CommandException.COMMAND_UNKNOWN_ARGUMENT.generateException(input.all(), pos);
            }
            return mappedValue;
        }

        @Override
        public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
            argument.processNodes(nodeMaker, executable);
        }
    }

    private static final class ArgumentFilter<T> extends Argument<T> {
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
        public @NotNull T parse(@NotNull StringReader input) throws CommandException {
            int pos = input.position();
            T result = argument.parse(input);
            if (!predicate.test(result)) {
                // TODO: Consider throwing a more accurate exception
                throw CommandException.COMMAND_UNKNOWN_ARGUMENT.generateException(input.all(), pos);
            }
            return result;
        }

        @Override
        public void processNodes(@NotNull NodeMaker nodeMaker, boolean executable) {
            argument.processNodes(nodeMaker, executable);
        }
    }
}
