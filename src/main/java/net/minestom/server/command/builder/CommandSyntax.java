package net.minestom.server.command.builder;

import net.minestom.server.command.builder.arguments.Argument;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a syntax in {@link Command}
 * which is initialized with {@link Command#addSyntax(CommandExecutor, Argument[])}.
 */
public class CommandSyntax {

    private final Argument<?>[] args;
    private CommandExecutor executor;

    protected CommandSyntax(@NotNull CommandExecutor commandExecutor, @NotNull Argument<?>... args) {
        this.executor = commandExecutor;
        this.args = args;
    }

    /**
     * Gets all the required {@link Argument} for this syntax.
     *
     * @return the required arguments
     */
    @NotNull
    public Argument<?>[] getArguments() {
        return args;
    }

    /**
     * Gets the {@link CommandExecutor} of this syntax, executed once the syntax is properly written.
     *
     * @return the executor of this syntax
     */
    @NotNull
    public CommandExecutor getExecutor() {
        return executor;
    }

    /**
     * Changes the {@link CommandExecutor} of this syntax.
     *
     * @param executor the new executor
     */
    public void setExecutor(@NotNull CommandExecutor executor) {
        this.executor = executor;
    }

}
