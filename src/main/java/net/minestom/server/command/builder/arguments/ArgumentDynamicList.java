package net.minestom.server.command.builder.arguments;

import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.entity.Player;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

/**
 * Represents a single word in the command, with valid words dynamically provided by a function.
 * <p>
 * <strong>Important</strong>: the dynamic list is sent exactly once (automatically) when the player joins, for use in tab completion.
 * The return value of {@link #getDynamicRestrictions(CommandSender)} will not be re-evaluated until you manually refresh the player's
 * command list with {@link Player#refreshCommands()}. However, server-side validation will still work as normal, obtaining fresh values
 * each time the argument is parsed.
 * <p>
 * Example: hey (if "hey" is in the list returned by the function)
 */
public class ArgumentDynamicList extends Argument<String> {

    public static final int SPACE_ERROR = 1;
    public static final int RESTRICTION_ERROR = 2;

    protected Function<CommandSender, List<String>> dynamicRestrictions;

    public ArgumentDynamicList(String id) {
        super(id);
    }

    /**
     * Sets the function to get the dynamic restrictions. Read the class Javadoc ({@link ArgumentDynamicList}) for more information.
     * <p>
     * <strong>Important</strong>: if this function returns a very large list, the packet sent to the player could exceed limits
     * or consume significant resources. It is recommended to implement a size limit within the function, but this is the caller's responsibility.
     *
     * @param dynamicRestrictions the function to get the dynamic restrictions
     * @return this argument, for chaining
     */
    public ArgumentDynamicList from(Function<CommandSender, List<String>> dynamicRestrictions) {
        this.dynamicRestrictions = dynamicRestrictions;
        return this;
    }

    @Override
    public String parse(CommandSender sender, String input) throws ArgumentSyntaxException {
        if (input.contains(StringUtils.SPACE))
            throw new ArgumentSyntaxException("Word cannot contain space character", input, SPACE_ERROR);

        // Get dynamic restrictions
        List<String> restrictions = dynamicRestrictions.apply(sender);
        if (restrictions != null && !restrictions.isEmpty()) {
            for (String r : restrictions) {
                if (input.equals(r))
                    return input;
            }
            throw new ArgumentSyntaxException("Word needs to be in the dynamic restriction list", input, RESTRICTION_ERROR);
        }

        return input; // If no restrictions, accept any word
    }

    @Override
    public ArgumentParserType parser() {
        return ArgumentParserType.STRING;
    }

    @Override
    public byte @Nullable [] nodeProperties() {
        return NetworkBuffer.makeArray(NetworkBuffer.VAR_INT, 0); // Single word
    }

    public List<String> getDynamicRestrictions(CommandSender sender) {
        return dynamicRestrictions.apply(sender);
    }

    @Override
    public String toString() {
        return String.format("DynamicList<%s>", getId());
    }
}
