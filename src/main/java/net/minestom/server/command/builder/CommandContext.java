package net.minestom.server.command.builder;

import net.minestom.server.command.builder.arguments.Argument;
import org.jetbrains.annotations.NotNull;

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

    public CommandContext(@NotNull String input) {
        this.input = input;
    }

    @NotNull
    public String getInput() {
        return input;
    }
}
