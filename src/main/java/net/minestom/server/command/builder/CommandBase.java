package net.minestom.server.command.builder;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.condition.*;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public abstract class CommandBase {
    private @Nullable CommandExecutor commandExecutor;
    private int executionCount = 0;
    private final Map<Class<? extends ExecuteCondition>, ExecuteCondition> executeConditions = new HashMap<>();
    private final Map<Class<? extends RemoveCondition>, RemoveCondition> removeConditions = new HashMap<>();
    private final Map<Class<? extends ComplexCondition>, ComplexCondition> complexConditions = new HashMap<>();
    protected final Set<Command> parents = new HashSet<>();
    protected boolean autoUpdate = true;

    private static boolean issuedWarning = false;
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandBase.class);
    private static final Set<CommandBase> commandBases = new HashSet<>();

    ///////////////////////////////////////////////////////////////////////////
    // Unregister self and cleanup
    ///////////////////////////////////////////////////////////////////////////

    public static void runCleanup() {
        final List<CommandBase> toRemove = commandBases.stream()
                .filter(x -> x.getRemoveConditions().stream().anyMatch(RemoveCondition::shouldRemove))
                .collect(Collectors.toList());
        if (toRemove.size() > 0) {
            final HashSet<Player> players = new HashSet<>(MinecraftServer.getConnectionManager().getOnlinePlayers());
            toRemove.stream().map(CommandBase::unregisterSelf).forEach(players::retainAll);
            MinecraftServer.getCommandManager().updateDeclaredCommands(players);
        }
    }

    @ApiStatus.Internal
    public Collection<Player> unregisterSelf() {
        for (Command parent : parents) {
            if (parent == null) {
                MinecraftServer.getCommandManager().unregister((Command) this);
            } else {
                parent.unregisterCommandBase(this);
            }
        }
        commandBases.remove(this);

        // Return the players who should receive update
        return getExecuteConditions().stream()
                .map(x -> MinecraftServer.getConnectionManager().getOnlinePlayers().stream()
                        .filter(y -> x.canUse(y, null))
                        .collect(Collectors.toList()))
                .flatMap(Collection::stream)
                .collect(Collectors.toUnmodifiableSet());
    }

    void addParent(Command parent) {
        parents.add(parent);
    }

    void removeParent(Command parent) {
        parents.remove(parent);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Command/syntax execution
    ///////////////////////////////////////////////////////////////////////////

    public void setExecutor(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    public @Nullable CommandExecutor getExecutor() {
        if (commandExecutor == null) {
            return null;
        }
        return this::apply;
    }

    private void apply(@NotNull CommandSender sender, @NotNull CommandContext context) {
        if (commandExecutor != null) {
            executionCount++;
            commandExecutor.apply(sender, context);
        }
    }

    public int getExecutionCount() {
        return executionCount;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Handle conditions
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Toggle automatic command list updating on condition change
     * <p>
     * Default: {@code true}
     * @param autoUpdate set to {@code false} to disable
     */
    public void setAutoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
    }

    @SuppressWarnings("UnusedReturnValue")
    @Contract("_ -> this")
    public CommandBase addConditions(@NotNull ComplexCondition ...complexConditions) {
        addConditions(((ExecuteCondition[]) complexConditions));
        addConditions(((RemoveCondition[]) complexConditions));
        for (ComplexCondition complexCondition : complexConditions) {
            this.complexConditions.put(complexCondition.getClass(), complexCondition);
        }
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    @Contract("_ -> this")
    public CommandBase addConditions(@NotNull ExecuteCondition ...executeConditions) {
        addConditions(this.executeConditions, executeConditions);
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    @Contract("_ -> this")
    public CommandBase addConditions(@NotNull RemoveCondition ...removeConditions) {
        addConditions(this.removeConditions, removeConditions);
        return this;
    }

    private void addConditions(Map map, Condition[] conditions) {
        Collection<Player> players = new HashSet<>(MinecraftServer.getConnectionManager().getOnlinePlayers());
        for (Condition condition : conditions) {
            Class<?> key = condition.getClass();
            if (map.containsKey(key) && !issuedWarning) {
                LOGGER.warn("A condition with the same type is already present, this action will override the existing condition! (This warning is only printed once.)");
                issuedWarning = true;
            }
            // TODO: 2021. 07. 15. Weaker access check?
            map.put(key, condition);
            players.retainAll(condition.getPlayers());
        }
        if (autoUpdate) {
            MinecraftServer.getCommandManager().updateDeclaredCommands(players);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Condition> T getCondition(Class<T> clazz) {
        if (ComplexCondition.class.isAssignableFrom(clazz)) {
            return (T) complexConditions.get(clazz);
        } else if (ExecuteCondition.class.isAssignableFrom(clazz)) {
            return (T) executeConditions.get(clazz);
        } else if (RemoveCondition.class.isAssignableFrom(clazz)) {
            return (T) removeConditions.get(clazz);
        }
        throw new IllegalArgumentException();
    }

    public void removeCondition(Class<? extends Condition> clazz) {
        executeConditions.remove(clazz);
        removeConditions.remove(clazz);
        complexConditions.remove(clazz);
    }

    public Collection<ExecuteCondition> getExecuteConditions() {
        return executeConditions.values();
    }

    public Collection<RemoveCondition> getRemoveConditions() {
        return removeConditions.values();
    }

    public Map<Class<? extends ComplexCondition>, ComplexCondition> getComplexConditions() {
        return complexConditions;
    }

    /**
     * Sets the {@link CommandCondition}.
     *
     * @param commandCondition the new command condition, null to do not call anything
     * @deprecated Use #addConditions(...)
     */
    @Deprecated
    public void setCondition(@Nullable CommandCondition commandCondition) {
        if (commandCondition != null) {
            addConditions(commandCondition);
        }
    }
}
