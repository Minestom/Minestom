package net.minestom.server.command.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentLiteral;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.condition.CommandCondition;
import net.minestom.server.command.builder.condition.conditions.RemoverCondition;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.StringUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a command which has suggestion/auto-completion.
 * <p>
 * The command works using a list of valid syntaxes.
 * For instance we could build the command
 * "/health set Notch 50" into multiple argument types "/health [set/add/remove] [username] [integer]"
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

    public final static Logger LOGGER = LoggerFactory.getLogger(Command.class);

    private final String name;
    private final String[] aliases;
    private final String[] names;

    private CommandExecutor defaultExecutor;
    private final Map<Class<? extends CommandCondition>, CommandCondition> conditions = new HashMap<>();
    private final Map<Class<? extends CommandCondition>, CommandCondition> unmodifiableConditions = Collections.unmodifiableMap(conditions);

    private final List<Command> subcommands;
    private final List<CommandSyntax> syntaxes;

    private final Set<Command> parents;

    private static final Set<Command> commands = new HashSet<>();

    static {
        MinecraftServer.getSchedulerManager()
                .buildTask(() -> {
                    final List<Command> toRemove = commands.stream()
                            .filter(x -> x.conditions.values().stream()
                                    .filter(y -> y instanceof RemoverCondition)
                                    .anyMatch(y -> ((RemoverCondition)y).shouldRemove())).collect(Collectors.toList());
                    if (toRemove.size() > 0) {
                        MinecraftServer.getCommandManager().updateDeclaredCommands(
                                toRemove.stream().map(Command::unregisterSelf)
                                        .flatMap(Collection::stream).collect(Collectors.toList()));
                    }
                })
                .repeat(Duration.ofMinutes(1)).schedule();
    }

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
        this.parents = new HashSet<>();
        this.names = Stream.concat(Arrays.stream(aliases), Stream.of(name)).toArray(String[]::new);
        this.subcommands = new ArrayList<>();
        this.syntaxes = new ArrayList<>();

        commands.add(this);
    }

    /**
     * Creates a {@link Command} with a name and no alias.
     *
     * @param name the name of the command
     * @see #Command(String, String...)
     */
    public Command(@NotNull String name) {
        this(name, new String[0]);
    }


    @Nullable
    public <T extends CommandCondition> T getCondition(Class<T> clazz) {
        return (T) conditions.get(clazz);
    }

    public Map<Class<? extends CommandCondition>, CommandCondition> getConditions() {
        return unmodifiableConditions;
    }

    /**
     * Sets the {@link CommandCondition}.
     *
     * @param commandCondition the new command condition, null to do not call anything
     */
    @Deprecated
    public void setCondition(@Nullable CommandCondition commandCondition) {
        addConditions(commandCondition);
    }

    public void addConditions(CommandCondition ...conditions) {
        final Set<Player> affectedPlayers = new HashSet<>();
        for (CommandCondition condition : conditions) {
            this.conditions.put(condition.getClass(), condition);
            affectedPlayers.addAll(condition.getAffectedPlayers());
        }
        MinecraftServer.getCommandManager().updateDeclaredCommands(affectedPlayers);
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

    public void addSubcommand(@NotNull Command command) {
        this.subcommands.add(command);
        command.addParent(this);
    }

    public void removeSubcommand(Command command) {
        this.subcommands.remove(command);
        command.removeParent(this);
    }

    @NotNull
    public List<Command> getSubcommands() {
        return Collections.unmodifiableList(subcommands);
    }

    /**
     * Adds a new syntax in the command.
     * <p>
     * A syntax is simply a list of arguments and an executor called when successfully parsed.
     *
     * @param commandCondition the condition to use the syntax
     * @param executor         the executor to call when the syntax is successfully received
     * @param args             all the arguments of the syntax, the length needs to be higher than 0
     * @return the created {@link CommandSyntax syntaxes},
     * there can be multiple of them when optional arguments are used
     */
    @NotNull
    public Collection<CommandSyntax> addConditionalSyntax(@Nullable CommandCondition commandCondition,
                                                          @NotNull CommandExecutor executor,
                                                          @NotNull Argument<?>... args) {
        // Check optional argument(s)
        boolean hasOptional = false;
        {
            for (Argument<?> argument : args) {
                if (argument.isOptional()) {
                    hasOptional = true;
                }
                if (hasOptional && !argument.isOptional()) {
                    LOGGER.warn("Optional arguments are followed by a non-optional one, the default values will be ignored.");
                    hasOptional = false;
                    break;
                }
            }
        }

        if (!hasOptional) {
            final CommandSyntax syntax = new CommandSyntax(commandCondition, executor, args);
            this.syntaxes.add(syntax);
            return Collections.singleton(syntax);
        } else {
            List<CommandSyntax> optionalSyntaxes = new ArrayList<>();

            // the 'args' array starts by all the required arguments, followed by the optional ones
            List<Argument<?>> requiredArguments = new ArrayList<>();
            Map<String, Supplier<Object>> defaultValuesMap = new HashMap<>();
            boolean optionalBranch = false;
            int i = 0;
            for (Argument<?> argument : args) {
                final boolean isLast = ++i == args.length;
                if (argument.isOptional()) {
                    // Set default value
                    defaultValuesMap.put(argument.getId(), (Supplier<Object>) argument.getDefaultValue());

                    if (!optionalBranch && !requiredArguments.isEmpty()) {
                        // First optional argument, create a syntax with current cached arguments
                        final CommandSyntax syntax = new CommandSyntax(commandCondition, executor, defaultValuesMap,
                                requiredArguments.toArray(new Argument[0]));
                        optionalSyntaxes.add(syntax);
                        optionalBranch = true;
                    } else {
                        // New optional argument, save syntax with current cached arguments and save default value
                        final CommandSyntax syntax = new CommandSyntax(commandCondition, executor, defaultValuesMap,
                                requiredArguments.toArray(new Argument[0]));
                        optionalSyntaxes.add(syntax);
                    }
                }
                requiredArguments.add(argument);
                if (isLast) {
                    // Create the last syntax
                    final CommandSyntax syntax = new CommandSyntax(commandCondition, executor, defaultValuesMap,
                            requiredArguments.toArray(new Argument[0]));
                    optionalSyntaxes.add(syntax);
                }
            }

            this.syntaxes.addAll(optionalSyntaxes);
            return optionalSyntaxes;
        }
    }

    /**
     * Adds a new syntax without condition.
     *
     * @see #addConditionalSyntax(CommandCondition, CommandExecutor, Argument[])
     */
    public @NotNull Collection<CommandSyntax> addSyntax(@NotNull CommandExecutor executor, @NotNull Argument<?>... args) {
        return addConditionalSyntax(null, executor, args);
    }

    /**
     * Creates a syntax from a formatted string.
     * <p>
     * Currently in beta as the format is not final.
     *
     * @param executor the syntax executor
     * @param format   the syntax format
     * @return the newly created {@link CommandSyntax syntaxes}.
     */
    @ApiStatus.Experimental
    public @NotNull Collection<CommandSyntax> addSyntax(@NotNull CommandExecutor executor, @NotNull String format) {
        return addSyntax(executor, ArgumentType.generate(format));
    }

    /**
     * Gets the main command's name.
     *
     * @return the main command's name
     */
    public @NotNull String getName() {
        return name;
    }

    /**
     * Gets the command's aliases.
     *
     * @return the command aliases, can be null or empty
     */
    public @Nullable String[] getAliases() {
        return aliases;
    }

    /**
     * Gets all the possible names for this command.
     * <p>
     * Include {@link #getName()} and {@link #getAliases()}.
     *
     * @return this command names
     */
    public @NotNull String[] getNames() {
        return names;
    }

    /**
     * Gets the default {@link CommandExecutor} which is called when there is no argument
     * or if no corresponding syntax has been found.
     *
     * @return the default executor, null if not any
     * @see #setDefaultExecutor(CommandExecutor)
     */
    @Nullable
    public CommandExecutor getDefaultExecutor() {
        return defaultExecutor;
    }

    /**
     * Sets the default {@link CommandExecutor}.
     *
     * @param executor the new default executor, null to remove it
     * @see #getDefaultExecutor()
     */
    public void setDefaultExecutor(@Nullable CommandExecutor executor) {
        this.defaultExecutor = executor;
    }

    /**
     * Gets all the syntaxes of this command.
     *
     * @return a collection containing all this command syntaxes
     * @see #addSyntax(CommandExecutor, Argument[])
     */
    public @NotNull Collection<CommandSyntax> getSyntaxes() {
        return syntaxes;
    }

    /**
     * Called when a {@link CommandSender} executes this command before any syntax callback.
     * <p>
     * WARNING: the {@link CommandCondition} is not executed, and all the {@link CommandSyntax} are not checked,
     * this is called every time a {@link CommandSender} send a command which start by {@link #getName()} or {@link #getAliases()}.
     * <p>
     * Can be used if you wish to still suggest the player syntaxes but want to parse things mostly by yourself.
     *
     * @param sender  the {@link CommandSender}
     * @param context the UNCHECKED context of the command, some can be null even when unexpected
     * @param command the raw UNCHECKED received command
     */
    public void globalListener(@NotNull CommandSender sender, @NotNull CommandContext context, @NotNull String command) {
    }

    @ApiStatus.Experimental
    public @NotNull Set<String> getSyntaxesStrings() {
        Set<String> syntaxes = new HashSet<>();

        Consumer<String> syntaxConsumer = syntaxString -> {
            for (String name : getNames()) {
                final String syntax = name + StringUtils.SPACE + syntaxString;
                syntaxes.add(syntax);
            }
        };

        this.subcommands.forEach(subcommand -> subcommand.getSyntaxesStrings().forEach(syntaxConsumer));

        this.syntaxes.forEach(commandSyntax -> syntaxConsumer.accept(commandSyntax.getSyntaxString()));

        return syntaxes;
    }

    @ApiStatus.Experimental
    public @NotNull String getSyntaxesTree() {
        Node commandNode = new Node();
        commandNode.names.addAll(Arrays.asList(getNames()));

        // current node, literal = returned node
        BiFunction<Node, Set<String>, Node> findNode = (currentNode, literals) -> {

            for (Node node : currentNode.nodes) {
                final var names = node.names;

                // Verify if at least one literal is shared
                final boolean shared = names.stream().anyMatch(literals::contains);
                if (shared) {
                    names.addAll(literals);
                    return node;
                }
            }

            // Create a new node
            Node node = new Node();
            node.names.addAll(literals);
            currentNode.nodes.add(node);
            return node;
        };

        BiConsumer<CommandSyntax, Node> syntaxProcessor = (syntax, node) -> {
            List<String> arguments = new ArrayList<>();
            BiConsumer<Node, List<String>> addArguments = (n, args) -> {
                if (!args.isEmpty()) {
                    n.arguments.add(args);
                }
            };

            // true if all following arguments are not part of
            // the branching plant (literals)
            boolean branched = false;
            for (Argument<?> argument : syntax.getArguments()) {
                if (!branched) {
                    if (argument instanceof ArgumentLiteral) {
                        final String literal = argument.getId();

                        addArguments.accept(node, arguments);
                        arguments = new ArrayList<>();

                        node = findNode.apply(node, Collections.singleton(literal));
                        continue;
                    } else if (argument instanceof ArgumentWord) {
                        ArgumentWord argumentWord = (ArgumentWord) argument;
                        if (argumentWord.hasRestrictions()) {

                            addArguments.accept(node, arguments);
                            arguments = new ArrayList<>();

                            node = findNode.apply(node, Set.of(argumentWord.getRestrictions()));
                            continue;
                        }
                    }
                }
                branched = true;
                arguments.add(argument.toString());
            }
            addArguments.accept(node, arguments);
        };

        // Subcommands
        this.subcommands.forEach(command -> {
            final Node node = findNode.apply(commandNode, Set.of(command.getNames()));
            command.getSyntaxes().forEach(syntax -> syntaxProcessor.accept(syntax, node));
        });

        // Syntaxes
        this.syntaxes.forEach(syntax -> syntaxProcessor.accept(syntax, commandNode));

        JsonObject jsonObject = new JsonObject();
        processNode(commandNode, jsonObject);
        return jsonObject.toString();
    }

    public static boolean isValidName(@NotNull Command command, @NotNull String name) {
        for (String commandName : command.getNames()) {
            if (commandName.equals(name)) {
                return true;
            }
        }
        return false;
    }

    private void processNode(@NotNull Node node, @NotNull JsonObject jsonObject) {
        BiConsumer<String, Consumer<JsonArray>> processor = (s, consumer) -> {
            JsonArray array = new JsonArray();
            consumer.accept(array);
            if (array.size() != 0) {
                jsonObject.add(s, array);
            }
        };
        // Names
        processor.accept("names", array -> node.names.forEach(array::add));
        // Nodes
        processor.accept("nodes", array ->
                node.nodes.forEach(n -> {
                    JsonObject nodeObject = new JsonObject();
                    processNode(n, nodeObject);
                    array.add(nodeObject);
                }));
        // Arguments
        processor.accept("arguments", array ->
                node.arguments.forEach(arguments ->
                        array.add(String.join(StringUtils.SPACE, arguments))));
    }

    Collection<Player> unregisterSelf() {
        for (Command parent : parents) {
            if (parent == null) {
                MinecraftServer.getCommandManager().unregister(this);
            } else {
                parent.removeSubcommand(this);
            }
        }
        commands.remove(this);

        return conditions.values().stream().map(CommandCondition::getAffectedPlayers).flatMap(Collection::stream).collect(Collectors.toUnmodifiableSet());
    }

    void addParent(@Nullable Command command) {
        parents.add(command);
    }

    void removeParent(@Nullable Command command) {
        parents.remove(command);
    }

    private static final class Node {
        private final Set<String> names = new HashSet<>();
        private final Set<Node> nodes = new HashSet<>();
        private final List<List<String>> arguments = new ArrayList<>();
    }
}
