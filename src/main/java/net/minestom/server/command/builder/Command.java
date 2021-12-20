package net.minestom.server.command.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentLiteral;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.condition.CommandCondition;
import net.minestom.server.utils.StringUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
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
    private final List<String> aliases;
    private final List<String> names;
    private final List<String> formattedNames;

    private CommandExecutor defaultExecutor;
    private CommandCondition condition;

    private final List<Command> subcommands;
    private final List<Command> subcommandsView;

    private final List<CommandSyntax> syntaxes;
    private final List<CommandSyntax> syntaxesView;

    /**
     * Creates a {@link Command} with a name and one or multiple aliases.
     *
     * @param name    the name of the command
     * @param aliases the command aliases
     * @see #Command(String)
     */
    public Command(@NotNull String name, @Nullable String... aliases) {
        this.name = name;
        this.aliases = aliases == null ? List.of() : List.of(aliases);
        this.names = Stream.concat(Stream.of(name), this.aliases.stream()).toList();
        this.formattedNames = names.stream().map(s -> s.toLowerCase(Locale.ROOT)).toList();

        this.subcommands = new ArrayList<>();
        this.subcommandsView = Collections.unmodifiableList(subcommands);

        this.syntaxes = new ArrayList<>();
        this.syntaxesView = Collections.unmodifiableList(syntaxes);
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

    /**
     * Gets the {@link CommandCondition}.
     * <p>
     * It is called after the parsing and just before the execution no matter the syntax used and can be used to check permissions or
     * the {@link CommandSender} type.
     * <p>
     * Worth mentioning that the condition is also used to know if the command known from a player (at connection).
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
     * @see #getCondition()
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

    public void addSubcommand(@NotNull Command command) {
        this.subcommands.add(command);
    }

    @NotNull
    public List<Command> getSubcommands() {
        return subcommandsView;
    }

    /**
     * Adds a new syntax in the command.
     * <p>
     * A syntax is simply a list of arguments and an executor called when successfully parsed.
     *
     * @param condition the condition to use the syntax
     * @param executor         the executor to call when the syntax is successfully received
     * @param args             all the arguments of the syntax, the length needs to be higher than 0
     * @return the created {@link CommandSyntax syntaxes}. There can be multiple of them when optional arguments are
     *         used, and it may be immutable based on the syntaxes that were added
     */
    public @NotNull Collection<CommandSyntax> addConditionalSyntax(@Nullable CommandCondition condition,
                                                          @NotNull CommandExecutor executor,
                                                          @NotNull Argument<?> @NotNull ... args) {
        // Quickly escape if there are no arguments
        if (args.length == 0) {
            final CommandSyntax syntax = new CommandSyntax(List.of(), executor, condition);
            this.syntaxes.add(syntax);
            return Collections.singleton(syntax);
        }
        // If the last argument is not optional, we know there is only one valid syntax.
        if (!args[args.length - 1].isOptional()) {
            final CommandSyntax syntax = new CommandSyntax(List.of(args), executor, condition);
            this.syntaxes.add(syntax);

            // Print warnings for arguments that are optional, since we know that the last one is not
            for (Argument<?> argument : args) {
                if (argument.isOptional()) {
                    LOGGER.warn("There is an optional argument (with id \"" + argument.getId() + "\") followed by a " +
                            "non-optional one. The optional argument will be treated as non-optional.");
                }
            }
            return Collections.singleton(syntax);
        }

        int firstOptional = 0;
        boolean shouldWarn = false;

        for (int i = args.length - 1; i >= 0; i--) {
            Argument<?> arg = args[i];

            if (!arg.isOptional() && !shouldWarn) {
                shouldWarn = true;
                // We know that the argument after the current one is the last optional, since this is the first
                // non-optional one from the right.
                firstOptional = i + 1;
            }
            if (arg.isOptional() && shouldWarn) {
                LOGGER.warn("There is an optional argument (with id \"" + arg.getId() + "\") followed by a " +
                        "non-optional one. It will be treated as non-optional.");
            }
        }

        List<Argument<?>> currentArguments = new ArrayList<>();
        List<CommandSyntax> optionalSyntaxes = new ArrayList<>();
        Map<String, Supplier<Object>> defaultValuesMap = new HashMap<>();

        // Add a syntax that includes none of the optional ones.
        optionalSyntaxes.add(new CommandSyntax(Arrays.asList(args).subList(0, firstOptional), executor, condition, null));

        for (int i = 0; i < args.length; i++) {
            Argument<?> arg = args[i];

            currentArguments.add(arg);

            if (i < firstOptional) {
                continue;
            }

            // At this point, `arg` must be optional due to the check above
            //noinspection unchecked
            defaultValuesMap.put(arg.getId(), (Supplier<Object>) arg.getDefaultValue());

            // It's safe to put them directly into this constructor because CommandSyntax copies them before saving
            optionalSyntaxes.add(new CommandSyntax(currentArguments, executor, condition, defaultValuesMap));
        }

        this.syntaxes.addAll(optionalSyntaxes);
        return optionalSyntaxes;
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
    public @NotNull List<String> getAliases() {
        return aliases;
    }

    /**
     * Gets all the possible names for this command.
     * <p>
     * Include {@link #getName()} and {@link #getAliases()}.
     *
     * @return this command names
     */
    public @NotNull List<String> getNames() {
        return names;
    }

    /**
     * Gets all the possible names for this command, formatted for parsing. Currently, the names are just converted to
     * lowercase.<br>
     * This includes {@link #getName()} and {@link #getAliases()}.
     */
    public @NotNull List<String> getFormattedNames() {
        return formattedNames;
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
     * @return a list containing all this command syntaxes
     * @see #addSyntax(CommandExecutor, Argument[])
     */
    public @NotNull List<CommandSyntax> getSyntaxes() {
        return syntaxesView;
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
        commandNode.names.addAll(names);

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
                    } else if (argument instanceof ArgumentWord argumentWord) {
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
            final Node node = findNode.apply(commandNode, Set.copyOf(command.getNames()));
            command.getSyntaxes().forEach(syntax -> syntaxProcessor.accept(syntax, node));
        });

        // Syntaxes
        this.syntaxes.forEach(syntax -> syntaxProcessor.accept(syntax, commandNode));

        JsonObject jsonObject = new JsonObject();
        processNode(commandNode, jsonObject);
        return jsonObject.toString();
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

    private static final class Node {
        private final Set<String> names = new HashSet<>();
        private final Set<Node> nodes = new HashSet<>();
        private final List<List<String>> arguments = new ArrayList<>();
    }

}
