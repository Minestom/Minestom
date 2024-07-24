package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.StringUtils;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a single word in the command.
 * <p>
 * You can specify the valid words with {@link #from(String...)} (do not abuse it or the client will not be able to join).
 * <p>
 * Example: hey
 */
public class ArgumentWord extends Argument<String> {

    public static final int SPACE_ERROR = 1;
    public static final int RESTRICTION_ERROR = 2;

    protected String[] restrictions;

    public ArgumentWord(String id) {
        super(id);
    }

    /**
     * Used to force the use of a few precise words instead of complete freedom.
     * <p>
     * WARNING: having an array too long would result in a packet too big or the client being stuck during login.
     *
     * @param restrictions the accepted words,
     *                     can be null but if an array is passed
     *                     you need to ensure that it is filled with non-null values
     * @return 'this' for chaining
     * @throws NullPointerException if {@code restrictions} is not null but contains null value(s)
     */
    @NotNull
    public ArgumentWord from(@Nullable String... restrictions) {
        if (restrictions != null) {
            for (String restriction : restrictions) {
                Check.notNull(restriction,
                        "ArgumentWord restriction cannot be null, you can pass 'null' instead of an empty array");
            }
        }

        this.restrictions = restrictions;
        return this;
    }

    @NotNull
    @Override
    public String parse(@NotNull CommandSender sender, @NotNull String input) throws ArgumentSyntaxException {
        if (input.contains(StringUtils.SPACE))
            throw new ArgumentSyntaxException("Word cannot contain space character", input, SPACE_ERROR);

        // Check restrictions (acting as literal)
        if (hasRestrictions()) {
            for (String r : restrictions) {
                if (input.equals(r))
                    return input;
            }
            throw new ArgumentSyntaxException("Word needs to be in the restriction list", input, RESTRICTION_ERROR);
        }

        return input;
    }

    @Override
    public String parser() {
        return "brigadier:string";
    }

    @Override
    public byte @Nullable [] nodeProperties() {
        return NetworkBuffer.makeArray(buffer -> {
            buffer.write(NetworkBuffer.VAR_INT, 0); // Single word
        });
    }

    /**
     * Gets if this argument allow complete freedom in the word choice or if a list has been defined.
     *
     * @return true if the word selection is restricted
     */
    public boolean hasRestrictions() {
        return restrictions != null && restrictions.length > 0;
    }

    /**
     * Gets all the word restrictions.
     *
     * @return the word restrictions, can be null
     */
    @Nullable
    public String[] getRestrictions() {
        return restrictions;
    }

    @Override
    public String toString() {
        return String.format("Word<%s>", getId());
    }
}
