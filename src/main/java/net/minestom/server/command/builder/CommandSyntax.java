package net.minestom.server.command.builder;

import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.condition.CommandCondition;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Represents a syntax in {@link Command}
 * which is initialized with {@link Command#addSyntax(CommandExecutor, Argument[])}.
 */
public class CommandSyntax {

    private CommandCondition commandCondition;
    private CommandExecutor executor;

    private final Map<String, Supplier<Object>> defaultValuesMap;
    private final Argument<?>[] args;

    private final boolean suggestion;

    protected CommandSyntax(@Nullable CommandCondition commandCondition,
                            @NotNull CommandExecutor commandExecutor,
                            @Nullable Map<String, Supplier<Object>> defaultValuesMap,
                            @NotNull Argument<?>... args) {
        this.commandCondition = commandCondition;
        this.executor = commandExecutor;

        this.defaultValuesMap = defaultValuesMap;
        this.args = args;

        this.suggestion = Arrays.stream(args).anyMatch(Argument::hasSuggestion);
    }

    protected CommandSyntax(@Nullable CommandCondition commandCondition,
                            @NotNull CommandExecutor commandExecutor,
                            @NotNull Argument<?>... args) {
        this(commandCondition, commandExecutor, null, args);
    }

    /**
     * Gets the condition to use this syntax.
     *
     * @return this command condition, null if none
     */
    @Nullable
    public CommandCondition getCommandCondition() {
        return commandCondition;
    }

    /**
     * Changes the command condition of this syntax.
     * <p>
     * Be aware that changing the command condition will not automatically update players auto-completion.
     * You can create a new packet containing the changes with
     * {@link net.minestom.server.command.CommandManager#createDeclareCommandsPacket(Player)}.
     *
     * @param commandCondition the new command condition, null to remove it
     */
    public void setCommandCondition(@Nullable CommandCondition commandCondition) {
        this.commandCondition = commandCondition;
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

    @Nullable
    protected Map<String, Supplier<Object>> getDefaultValuesMap() {
        return defaultValuesMap;
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

    public boolean hasSuggestion() {
        return suggestion;
    }

    public @NotNull String getSyntaxString() {
        StringBuilder builder = new StringBuilder();
        for (Argument<?> argument : args) {
            builder.append(argument.toString())
                    .append(StringUtils.SPACE);
        }
        return builder.toString().trim();
    }
}
