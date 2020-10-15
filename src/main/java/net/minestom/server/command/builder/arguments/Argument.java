package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.builder.ArgumentCallback;
import net.minestom.server.command.builder.Command;

/**
 * An argument is meant to be parsed when added into a {@link Command} syntax.
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

    public Argument(String id, boolean allowSpace, boolean useRemaining) {
        this.id = id;
        this.allowSpace = allowSpace;
        this.useRemaining = useRemaining;
    }

    public Argument(String id, boolean allowSpace) {
        this(id, allowSpace, false);
    }

    public Argument(String id) {
        this(id, false, false);
    }

    /**
     * Used to provide the appropriate error concerning the received args.
     *
     * @param value The received argument
     * @return The success/error code
     */
    public abstract int getCorrectionResult(String value);

    /**
     * The argument syntax is correct, parsed here to the correct type.
     *
     * @param value The correct argument
     * @return The parsed argument
     */
    public abstract T parse(String value);

    /**
     * The argument is at least partially correct (the syntax is good and the argument has been parsed)
     * but some other conditions could take place (ex: min/max requirement for numbers).
     *
     * @param value The parsed argument
     * @return The success/error code
     */
    public abstract int getConditionResult(T value);

    /**
     * Get the ID of the argument, showed in-game above the chat bar
     * and used to retrieve the data when the command is parsed
     *
     * @return the argument id
     */
    public String getId() {
        return id;
    }

    /**
     * Get if the argument can contain space
     *
     * @return true if the argument allows space, false otherwise
     */
    public boolean allowSpace() {
        return allowSpace;
    }

    /**
     * Get if the argument always use all the remaining characters
     * <p>
     * ex: /help I am a test - would get you "I am a test"
     * if the sole argument does use the remaining
     *
     * @return true if the argument use all the remaining characters, false otherwise
     */
    public boolean useRemaining() {
        return useRemaining;
    }

    /**
     * Get the {@link ArgumentCallback} to check if the argument-specific conditions are validated or not.
     *
     * @return the argument callback
     */
    public ArgumentCallback getCallback() {
        return callback;
    }

    /**
     * Set the {@link ArgumentCallback}.
     *
     * @param callback the argument callback
     */
    public void setCallback(ArgumentCallback callback) {
        this.callback = callback;
    }

    /**
     * Get if the argument has any error callback
     *
     * @return true if the argument has an error callback, false otherwise
     */
    public boolean hasErrorCallback() {
        return callback != null;
    }

}
