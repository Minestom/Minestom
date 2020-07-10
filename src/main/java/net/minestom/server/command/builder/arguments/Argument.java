package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.builder.ArgumentCallback;

public abstract class Argument<T> {

    public static final int SUCCESS = 0;
    public static final int UNDEFINED_ERROR = -1;

    private String id;
    private boolean allowSpace;
    private boolean useRemaining;

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
     * Used to provide the appropriate error concerning the args received
     *
     * @param value The received argument
     * @return The success/error code
     */
    public abstract int getCorrectionResult(String value);

    /**
     * The argument syntax is correct, parsed here to the correct type
     *
     * @param value The correct argument
     * @return The parsed argument
     */
    public abstract T parse(String value);

    /**
     * Argument is at least partially correct (the syntax is good and the argument has been parsed)
     * but some other conditions could take place (ex: min/max requirement for numbers)
     *
     * @param value The parsed argument
     * @return The success/error code
     */
    public abstract int getConditionResult(T value);

    public String getId() {
        return id;
    }

    public boolean allowSpace() {
        return allowSpace;
    }

    public boolean useRemaining() {
        return useRemaining;
    }

    public ArgumentCallback getCallback() {
        return callback;
    }

    public void setCallback(ArgumentCallback callback) {
        this.callback = callback;
    }

    public boolean hasErrorCallback() {
        return callback != null;
    }

}
