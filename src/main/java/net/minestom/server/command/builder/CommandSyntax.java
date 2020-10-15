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
     * Get all the required {@link Argument} for this sytnax
     *
     * @return the required arguments
     */
    public Argument[] getArguments() {
        return args;
    }

    /**
     * Get the {@link CommandExecutor} of this syntax, executed once the syntax is properly wrote.
     *
     * @return the executor of this syntax
     */
    public CommandExecutor getExecutor() {
        return executor;
    }

    /**
     * Change the {@link CommandExecutor} of this syntax
     *
     * @param executor the new executor
     */
    public void setExecutor(CommandExecutor executor) {
        this.executor = executor;
    }

}
