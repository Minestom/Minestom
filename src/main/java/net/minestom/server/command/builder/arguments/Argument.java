package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.builder.ArgumentCallback;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An argument is meant to be parsed when added into a {@link Command}'s syntax with {@link Command#addSyntax(CommandExecutor, Argument[])}.
 * <p>
 * You can create your own with your own special conditions.
 * <p>
 * Here in order, how is parsed an argument: {@link #getCorrectionResult(String)} to check
 * if the syntax is correct, {@link #parse(String)} to convert the correct argument
 * and {@link #getConditionResult(Object)} to verify that the parsed object validate the additional
 * conditions.
 *
 * @param <T> the type of this parsed argument
 */
public abstract class Argument<T> {

    public static final int SUCCESS = 0;
    public static final int UNDEFINED_ERROR = -1;

    private final String id;
    private final boolean allowSpace;
    private final boolean useRemaining;

    private ArgumentCallback callback;

    public Argument(@NotNull String id, boolean allowSpace, boolean useRemaining) {
        this.id = id;
        this.allowSpace = allowSpace;
        this.useRemaining = useRemaining;
    }

    public Argument(@NotNull String id, boolean allowSpace) {
        this(id, allowSpace, false);
    }

    public Argument(@NotNull String id) {
        this(id, false, false);
    }

    /**
     * First method called to check the validity of an input.
     * <p>
     * If {@link #allowSpace()} is enabled, the value will be incremented by the next word until it returns {@link #SUCCESS},
     * meaning that you need to be sure to check the inexpensive operations first (eg the number of brackets, the first and last char, etc...).
     *
     * @param value The received argument
     * @return the error code or {@link #SUCCESS}
     */
    public abstract int getCorrectionResult(@NotNull String value);

    /**
     * Called after {@link #getCorrectionResult(String)} returned {@link #SUCCESS}.
     * <p>
     * The correction being correct means that {@code value} shouldn't be verified again, you can assume that no exception will occur
     * when converting it to the correct type.
     *
     * @param value The correct argument which does not need to be verified again
     * @return The parsed argument
     */
    @NotNull
    public abstract T parse(@NotNull String value);

    /**
     * Called after {@link #parse(String)} meaning that {@code value} should already represent a valid representation of the input.
     * <p>
     * The condition result has for goal to check the optional conditions that are user configurable (eg min/max values for a number, a specific material
     * for an item, etc...).
     *
     * @param value The parsed argument
     * @return the error code or {@link #SUCCESS}
     */
    public abstract int getConditionResult(@NotNull T value);

    /**
     * Gets the ID of the argument, showed in-game above the chat bar
     * and used to retrieve the data when the command is parsed in {@link net.minestom.server.command.builder.Arguments}.
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
     * ex: /help I am a test - would get you "I am a test"
     * if the sole argument does use the remaining.
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

}
