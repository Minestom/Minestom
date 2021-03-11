package net.minestom.server.command.builder;

import net.minestom.server.command.builder.arguments.Argument;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Class used to retrieve argument data in a {@link CommandExecutor}.
 * <p>
 * All id are the one specified in the {@link Argument} constructor.
 * <p>
 * All methods are @{@link NotNull} in the sense that you should not have to verify their validity since if the syntax
 * is called, it means that all of its arguments are correct. Be aware that trying to retrieve an argument not present
 * in the syntax will result in a {@link NullPointerException}.
 */
public class CommandContext extends Arguments {

    private final String input;
    private final String commandName;
    private final Map<String, String> rawArgs = new HashMap<>();

    public CommandContext(@NotNull String input) {
        this.input = input;
        this.commandName = input.split(StringUtils.SPACE)[0];
    }

    @NotNull
    public String getInput() {
        return input;
    }

    @NotNull
    public String getCommandName() {
        return commandName;
    }

    @NotNull
    public <T> T getRaw(@NotNull Argument<T> argument) {
        return get(argument.getId());
    }

    public <T> T getRaw(@NotNull String identifier) {
        return (T) rawArgs.computeIfAbsent(identifier, s -> {
            throw new NullPointerException(
                    "The argument with the id '" + identifier + "' has no value assigned, be sure to check your arguments id, your syntax, and that you do not change the argument id dynamically.");
        });
    }

    public void setArg(@NotNull String id, Object value, String rawInput) {
        this.args.put(id, value);
        this.rawArgs.put(id, rawInput);
    }


}
