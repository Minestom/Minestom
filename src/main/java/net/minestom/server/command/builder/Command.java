package net.minestom.server.command.builder;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentDynamicStringArray;
import net.minestom.server.command.builder.arguments.ArgumentDynamicWord;
import net.minestom.server.command.builder.condition.CommandCondition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a command which have suggestion/auto-completion
 */
public class Command {

    private String name;
    private String[] aliases;

    private CommandExecutor defaultExecutor;
    private CommandCondition condition;
    private List<CommandSyntax> syntaxes;

    public Command(String name, String... aliases) {
        this.name = name;
        this.aliases = aliases;

        this.syntaxes = new ArrayList<>();
    }

    public Command(String name) {
        this(name, new String[0]);
    }

    /**
     * Get the command condition
     * <p>
     * It is called no matter the syntax used and can be used to check permissions or
     * the {@link CommandSender} type
     *
     * @return the command condition
     */
    public CommandCondition getCondition() {
        return condition;
    }

    /**
     * Set the command condition
     *
     * @param commandCondition the new command condition
     */
    public void setCondition(CommandCondition commandCondition) {
        this.condition = commandCondition;
    }

    /**
     * Add an argument callback
     * <p>
     * The argument callback is called when there's an error in the argument
     *
     * @param callback the callback for the argument
     * @param argument the argument which get the callback
     */
    public void addCallback(ArgumentCallback callback, Argument argument) {
        argument.setCallback(callback);
    }

    /**
     * Add a new syntax in the command
     * <p>
     * A syntax is simply a list of arguments
     *
     * @param executor the executor to call when the syntax is successfully received
     * @param args     all the arguments of the syntax
     */
    public void addSyntax(CommandExecutor executor, Argument... args) {
        CommandSyntax syntax = new CommandSyntax(args);
        syntax.setExecutor(executor);
        this.syntaxes.add(syntax);
    }

    /**
     * Get the main command's name
     *
     * @return the main command's name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the command's aliases
     * <p>
     * Can be null or empty
     *
     * @return the command aliases
     */
    public String[] getAliases() {
        return aliases;
    }

    /**
     * Get the default executor (which is called when there is no argument)
     * or if no corresponding syntax has been found
     *
     * @return the default executor
     */
    public CommandExecutor getDefaultExecutor() {
        return defaultExecutor;
    }

    /**
     * Set the default executor (which is called when there is no argument)
     *
     * @param executor the new default executor
     */
    public void setDefaultExecutor(CommandExecutor executor) {
        this.defaultExecutor = executor;
    }

    /**
     * Get all the syntaxes of this command
     *
     * @return a collection containing all this command syntaxes
     */
    public Collection<CommandSyntax> getSyntaxes() {
        return syntaxes;
    }

    /**
     * Allow for tab auto completion, this is called everytime the player press a key in the chat
     * when in a dynamic argument ({@link ArgumentDynamicWord} and {@link ArgumentDynamicStringArray})
     *
     * @param text the whole player text
     * @return the array containing all the suggestion for the current arg (split " ")
     */
    public String[] onDynamicWrite(String text) {
        return null;
    }

    /**
     * Called when a {@link CommandSender} executes this command.
     * Executed before any syntax callback.
     * <p>
     * WARNING: the {@link CommandCondition} is not executed, and all the {@link CommandSyntax} are not checked,
     * this is called every time a {@link CommandSender} send a command which start by {@link #getName()} or {@link #getAliases()}.
     * <p>
     * Can be used if you wish to still suggest the player syntaxes but want to parse things mostly by yourself.
     *
     * @param sender    the {@link CommandSender}
     * @param arguments the UNCHECKED arguments of the command, some can be null even when unexpected
     * @param command   the raw UNCHECKED received command
     */
    public void globalListener(CommandSender sender, Arguments arguments, String command) {
    }

}
