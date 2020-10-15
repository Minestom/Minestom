package net.minestom.server.command.builder;

import net.minestom.server.command.builder.arguments.Argument;

/**
 * Represents a syntax in {@link Command}.
 */
public class CommandSyntax {

    private final Argument[] args;
    private CommandExecutor executor;

    protected CommandSyntax(Argument... args) {
        this.args = args;
    }

    /**
     * Gets all the required {@link Argument} for this syntax.
     *
     * @return the required arguments
     */
    public Argument[] getArguments() {
        return args;
    }

    /**
     * Gets the {@link CommandExecutor} of this syntax, executed once the syntax is properly written.
     *
     * @return the executor of this syntax
     */
    public CommandExecutor getExecutor() {
        return executor;
    }

    /**
     * Changes the {@link CommandExecutor} of this syntax.
     *
     * @param executor the new executor
     */
    public void setExecutor(CommandExecutor executor) {
        this.executor = executor;
    }

}
