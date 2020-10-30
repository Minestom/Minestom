package net.minestom.server.command.builder;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentDynamicStringArray;
import net.minestom.server.command.builder.arguments.ArgumentDynamicWord;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.condition.CommandCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a command which have suggestion/auto-completion.
 * <p>
 * The command works using a list of valid syntaxes.
 * For instance we could build the command
 * "/health set Notch 50" into multiple argument types "/health [set/add/remove] [Username] [Integer]"
 * <p>
 * All the default argument types can be found in {@link ArgumentType}
 * and the syntax be created/registered using {@link #addSyntax(CommandExecutor, Argument[])}.
 * <p>
 * If the command is executed with an incorrect syntax or without any argument, the default {@link CommandExecutor} will be called,
 * you can set it using {@link #setDefaultExecutor(CommandExecutor)}.
 * <p>
 * Before any syntax to be successfully executed the {@link CommandSender} needs to validated
 * the {@link CommandCondition} sets with {@link #setCondition(CommandCondition)} (ignored if null).
 * <p>
 * Some {@link Argument} could also require additional condition (eg: a number which need to be between 2 values),
 * in this case, if the whole syntax is correct but not the argument condition,
 * you can listen to its error code using {@link #setArgumentCallback(ArgumentCallback, Argument)} or {@link Argument#setCallback(ArgumentCallback)}.
 */
public class Command {

    private final String name;
    private final String[] aliases;

    private CommandExecutor defaultExecutor;
    private CommandCondition condition;

    private final List<CommandSyntax> syntaxes;

    /**
     * Creates a {@link Command} with a name and one or multiple aliases.
     *
     * @param name    the name of the command
     * @param aliases the command aliases
     * @see #Command(String)
     */
    public Command(@NotNull String name, @Nullable String... aliases) {
        this.name = name;
        this.aliases = aliases;

        this.syntaxes = new ArrayList<>();
    }

    /**
     * Creates a {@link Command} with a name without any aliases.
     *
     * @param name the name of the command
     * @see #Command(String, String...)
     */
    public Command(@NotNull String name) {
        this(name, new String[0]);
    }

    /**
     * Gets the {@link CommandCondition}.
     * <p>
     * It is called no matter the syntax used and can be used to check permissions or
     * the {@link CommandSender} type.
     *
     * @return the command condition, null if not any
     */
    @Nullable
    public CommandCondition getCondition() {
        return condition;
    }

    /**
     * Sets the {@link CommandCondition}.
     *
     * @param commandCondition the new command condition, null to do not call anything
     */
    public void setCondition(@Nullable CommandCondition commandCondition) {
        this.condition = commandCondition;
    }

    /**
     * Sets an {@link ArgumentCallback}.
     * <p>
     * The argument callback is called when there's an error in the argument.
     *
     * @param callback the callback for the argument
     * @param argument the argument which get the callback
     */
    public void setArgumentCallback(@NotNull ArgumentCallback callback, @NotNull Argument<?> argument) {
        argument.setCallback(callback);
    }

    /**
     * Adds a new syntax in the command.
     * <p>
     * A syntax is simply a list of arguments
     *
     * @param executor the executor to call when the syntax is successfully received
     * @param args     all the arguments of the syntax
     * @return the created {@link CommandSyntax}
     */
    public CommandSyntax addSyntax(@NotNull CommandExecutor executor, @NotNull Argument<?>... args) {
        final CommandSyntax syntax = new CommandSyntax(executor, args);
        this.syntaxes.add(syntax);
        return syntax;
    }

    /**
     * Gets the main command's name.
     *
     * @return the main command's name
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Gets the command's aliases.
     *
     * @return the command aliases, can be null or empty
     */
    @Nullable
    public String[] getAliases() {
        return aliases;
    }

    /**
     * Gets the default {@link CommandExecutor} which is called when there is no argument
     * or if no corresponding syntax has been found.
     *
     * @return the default executor, null if not any
     */
    @Nullable
    public CommandExecutor getDefaultExecutor() {
        return defaultExecutor;
    }

    /**
     * Sets the default {@link CommandExecutor} (which is called when there is no argument).
     *
     * @param executor the new default executor, null to remove it
     */
    public void setDefaultExecutor(@Nullable CommandExecutor executor) {
        this.defaultExecutor = executor;
    }

    /**
     * Gets all the syntaxes of this command.
     *
     * @return a collection containing all this command syntaxes
     */
    @NotNull
    public Collection<CommandSyntax> getSyntaxes() {
        return syntaxes;
    }

    /**
     * Allows for tab auto completion, this is called everytime the player press a key in the chat
     * when in a dynamic argument ({@link ArgumentDynamicWord} and {@link ArgumentDynamicStringArray}).
     *
     * @param text the whole player's text
     * @return the array containing all the suggestion for the current arg (split " "), can be null
     */
    @Nullable
    public String[] onDynamicWrite(@NotNull String text) {
        return null;
    }

    /**
     * Called when a {@link CommandSender} executes this command before any syntax callback.
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
    public void globalListener(@NotNull CommandSender sender, @NotNull Arguments arguments, @NotNull String command) {
    }

}
