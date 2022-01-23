package net.minestom.server.command;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minestom.server.command.builder.*;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.minecraft.SuggestionType;
import net.minestom.server.command.builder.condition.CommandCondition;
import net.minestom.server.command.builder.parser.ArgumentQueryResult;
import net.minestom.server.command.builder.parser.CommandParser;
import net.minestom.server.command.builder.parser.CommandQueryResult;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.player.PlayerCommandEvent;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.utils.ArrayUtils;
import net.minestom.server.utils.callback.CommandCallback;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Manager used to register {@link Command commands}.
 * <p>
 * It is also possible to simulate a command using {@link #execute(CommandSender, String)}.
 */
public final class CommandManager {

    public static final String COMMAND_PREFIX = "/";

    private final ServerSender serverSender = new ServerSender();
    private final ConsoleSender consoleSender = new ConsoleSender();

    private final CommandDispatcher dispatcher = new CommandDispatcher();

    private CommandCallback unknownCommandCallback;

    public CommandManager() {
    }

    /**
     * Registers a {@link Command}.
     *
     * @param command the command to register
     * @throws IllegalStateException if a command with the same name already exists
     */
    public synchronized void register(@NotNull Command command) {
        Check.stateCondition(commandExists(command.getName()),
                "A command with the name " + command.getName() + " is already registered!");
        if (command.getAliases() != null) {
            for (String alias : command.getAliases()) {
                Check.stateCondition(commandExists(alias),
                        "A command with the name " + alias + " is already registered!");
            }
        }
        this.dispatcher.register(command);
    }

    /**
     * Removes a command from the currently registered commands.
     * Does nothing if the command was not registered before
     *
     * @param command the command to remove
     */
    public void unregister(@NotNull Command command) {
        this.dispatcher.unregister(command);
    }

    /**
     * Gets the {@link Command} registered by {@link #register(Command)}.
     *
     * @param commandName the command name
     * @return the command associated with the name, null if not any
     */
    public @Nullable Command getCommand(@NotNull String commandName) {
        return dispatcher.findCommand(commandName);
    }

    /**
     * Gets if a command with the name {@code commandName} already exists or not.
     *
     * @param commandName the command name to check
     * @return true if the command does exist
     */
    public boolean commandExists(@NotNull String commandName) {
        commandName = commandName.toLowerCase();
        return dispatcher.findCommand(commandName) != null;
    }

    /**
     * Executes a command for a {@link CommandSender}.
     *
     * @param sender  the sender of the command
     * @param command the raw command string (without the command prefix)
     * @return the execution result
     */
    public @NotNull CommandResult execute(@NotNull CommandSender sender, @NotNull String command) {
        // Command event
        if (sender instanceof Player player) {
            PlayerCommandEvent playerCommandEvent = new PlayerCommandEvent(player, command);
            EventDispatcher.call(playerCommandEvent);
            if (playerCommandEvent.isCancelled())
                return CommandResult.of(CommandResult.Type.CANCELLED, command);
            command = playerCommandEvent.getCommand();
        }
        // Process the command
        final CommandResult result = dispatcher.execute(sender, command);
        if (result.getType() == CommandResult.Type.UNKNOWN) {
            if (unknownCommandCallback != null) {
                this.unknownCommandCallback.apply(sender, command);
            }
        }
        return result;
    }

    /**
     * Executes the command using a {@link ServerSender}. This can be used
     * to run a silent command (nothing is printed to console).
     *
     * @see #execute(CommandSender, String)
     */
    public @NotNull CommandResult executeServerCommand(@NotNull String command) {
        return execute(serverSender, command);
    }

    public @NotNull CommandDispatcher getDispatcher() {
        return dispatcher;
    }

    /**
     * Gets the callback executed once an unknown command is run.
     *
     * @return the unknown command callback, null if not any
     */
    public @Nullable CommandCallback getUnknownCommandCallback() {
        return unknownCommandCallback;
    }

    /**
     * Sets the callback executed once an unknown command is run.
     *
     * @param unknownCommandCallback the new unknown command callback,
     *                               setting it to null mean that nothing will be executed
     */
    public void setUnknownCommandCallback(@Nullable CommandCallback unknownCommandCallback) {
        this.unknownCommandCallback = unknownCommandCallback;
    }

    /**
     * Gets the {@link ConsoleSender} (which is used as a {@link CommandSender}).
     *
     * @return the {@link ConsoleSender}
     */
    public @NotNull ConsoleSender getConsoleSender() {
        return consoleSender;
    }

    /**
     * Gets the {@link DeclareCommandsPacket} for a specific player.
     * <p>
     * Can be used to update a player auto-completion list.
     *
     * @param player the player to get the commands packet
     * @return the {@link DeclareCommandsPacket} for {@code player}
     */
    public @NotNull DeclareCommandsPacket createDeclareCommandsPacket(@NotNull Player player) {
        return buildPacket(player);
    }

    /**
     * Builds the {@link DeclareCommandsPacket} for a {@link Player}.
     *
     * @param player the player to build the packet for
     * @return the commands packet for the specific player
     */
    private @NotNull DeclareCommandsPacket buildPacket(@NotNull Player player) {
        List<DeclareCommandsPacket.Node> nodes = new ArrayList<>();
        // Contains the children of the main node (all commands name)
        IntList rootChildren = new IntArrayList();

        // Root node
        DeclareCommandsPacket.Node rootNode = new DeclareCommandsPacket.Node();
        rootNode.flags = 0;
        nodes.add(rootNode);

        Map<Command, Integer> commandIdentityMap = new IdentityHashMap<>();
        Map<Argument<?>, Integer> argumentIdentityMap = new IdentityHashMap<>();

        List<Pair<String, NodeMaker.Request>> nodeRequests = new ArrayList<>();

        // Brigadier-like commands
        for (Command command : dispatcher.getCommands()) {
            final int commandNodeIndex = serializeCommand(player, command, nodes, rootChildren, commandIdentityMap, argumentIdentityMap, nodeRequests);
            commandIdentityMap.put(command, commandNodeIndex);
        }

        // Answer to all node requests
        for (Pair<String, NodeMaker.Request> pair : nodeRequests) {
            String input = pair.left();
            NodeMaker.Request request = pair.right();

            final CommandQueryResult commandQueryResult = CommandParser.findCommand(dispatcher, input);
            if (commandQueryResult == null) {
                // Invalid command, return root node
                request.retrieve(0);
                continue;
            }

            final ArgumentQueryResult queryResult = CommandParser.findEligibleArgument(commandQueryResult.command(),
                    commandQueryResult.args(), input, false, true, syntax -> true, argument -> true);
            if (queryResult == null) {
                // Invalid argument, return command node (default to root)
                final int commandNode = commandIdentityMap.getOrDefault(commandQueryResult.command(), 0);
                request.retrieve(commandNode);
                continue;
            }

            // Retrieve argument node
            final int argumentNode = argumentIdentityMap.getOrDefault(queryResult.argument(), 0);
            request.retrieve(argumentNode);
        }
        // Add root node children
        rootNode.children = ArrayUtils.toArray(rootChildren);
        return new DeclareCommandsPacket(nodes, 0);
    }

    private int serializeCommand(CommandSender sender, Command command,
                                 List<DeclareCommandsPacket.Node> nodes,
                                 IntList rootChildren,
                                 Map<Command, Integer> commandIdentityMap,
                                 Map<Argument<?>, Integer> argumentIdentityMap,
                                 List<Pair<String, NodeMaker.Request>> nodeRequests) {
        // Check if player should see this command
        final CommandCondition commandCondition = command.getCondition();
        if (commandCondition != null) {
            // Do not show command if return false
            if (!commandCondition.canUse(sender, null)) {
                return -1;
            }
        }

        // The main root of this command
        IntList cmdChildren = new IntArrayList();
        final Collection<CommandSyntax> syntaxes = command.getSyntaxes();

        // Create command for main name
        final DeclareCommandsPacket.Node mainNode = createCommandNodes(sender, nodes, cmdChildren,
                command.getName(), syntaxes, rootChildren, argumentIdentityMap, nodeRequests);
        final int mainNodeIndex = nodes.indexOf(mainNode);

        // Serialize all the subcommands
        for (Command subcommand : command.getSubcommands()) {
            final int subNodeIndex = serializeCommand(sender, subcommand, nodes, cmdChildren, commandIdentityMap, argumentIdentityMap, nodeRequests);
            if (subNodeIndex != -1) {
                mainNode.children = ArrayUtils.concatenateIntArrays(mainNode.children, new int[]{subNodeIndex});
                commandIdentityMap.put(subcommand, subNodeIndex);
            }
        }

        // Use redirection to hook aliases with the command
        final String[] aliases = command.getAliases();
        if (aliases != null) {
            for (String alias : aliases) {
                DeclareCommandsPacket.Node aliasNode = new DeclareCommandsPacket.Node();
                aliasNode.flags = DeclareCommandsPacket.getFlag(DeclareCommandsPacket.NodeType.LITERAL,
                        false, true, false);
                aliasNode.name = alias;
                aliasNode.redirectedNode = mainNodeIndex;

                addCommandNameNode(aliasNode, rootChildren, nodes);
            }
        }

        return mainNodeIndex;
    }

    /**
     * Adds the command's syntaxes to the nodes list.
     *
     * @param sender       the potential sender of the command
     * @param nodes        the nodes of the packet
     * @param cmdChildren  the main root of this command
     * @param name         the name of the command (or the alias)
     * @param syntaxes     the syntaxes of the command
     * @param rootChildren the children of the main node (all commands name)
     * @return The index of the main node for alias redirection
     */
    private DeclareCommandsPacket.Node createCommandNodes(@NotNull CommandSender sender,
                                                          @NotNull List<DeclareCommandsPacket.Node> nodes,
                                                          @NotNull IntList cmdChildren,
                                                          @NotNull String name,
                                                          @NotNull Collection<CommandSyntax> syntaxes,
                                                          @NotNull IntList rootChildren,
                                                          @NotNull Map<Argument<?>, Integer> argumentIdentityMap,
                                                          @NotNull List<Pair<String, NodeMaker.Request>> nodeRequests) {

        DeclareCommandsPacket.Node literalNode = createMainNode(name, syntaxes.isEmpty());

        final int literalNodeId = addCommandNameNode(literalNode, rootChildren, nodes);

        // Contains the arguments of the already-parsed syntaxes
        Map<CommandSyntax, Argument<?>[]> syntaxesArguments = new HashMap<>();
        // Contains the nodes of an argument
        Map<IndexedArgument, List<DeclareCommandsPacket.Node[]>> storedArgumentsNodes = new HashMap<>();

        // Sort syntaxes by argument count. Brigadier requires it.
        syntaxes = syntaxes.stream().sorted(Comparator.comparingInt(o -> -o.getArguments().length)).toList();
        for (CommandSyntax syntax : syntaxes) {
            final CommandCondition commandCondition = syntax.getCommandCondition();
            if (commandCondition != null && !commandCondition.canUse(sender, null)) {
                // Sender does not have the right to use this syntax, ignore it
                continue;
            }

            // Represent the last nodes computed in the last iteration
            DeclareCommandsPacket.Node[] lastNodes = new DeclareCommandsPacket.Node[]{literalNode};

            // Represent the children of the last node
            IntList argChildren = cmdChildren;

            NodeMaker nodeMaker = new NodeMaker(lastNodes, literalNodeId);
            int lastArgumentNodeIndex = nodeMaker.getNodesCount();

            final Argument<?>[] arguments = syntax.getArguments();
            for (int i = 0; i < arguments.length; i++) {
                final Argument<?> argument = arguments[i];
                final boolean isLast = i == arguments.length - 1;

                // Search previously parsed syntaxes to find identical part in order to create a link between those
                {
                    // Find shared part
                    boolean foundSharedPart = false;
                    for (var entry : syntaxesArguments.entrySet()) {
                        final var parsedArguments = entry.getValue();
                        final int index = i + 1;
                        if (Arrays.mismatch(arguments, 0, index, parsedArguments, 0, index) == -1) {
                            final Argument<?> sharedArgument = parsedArguments[i];
                            final var sharedSyntax = entry.getKey();
                            final var indexed = new IndexedArgument(sharedSyntax, sharedArgument, i);
                            final List<DeclareCommandsPacket.Node[]> storedNodes = storedArgumentsNodes.get(indexed);
                            if (storedNodes == null)
                                continue; // Retrieved argument has already been redirected

                            argChildren = new IntArrayList();
                            lastNodes = storedNodes.get(storedNodes.size() > index ? index : i);
                            foundSharedPart = true;
                        }
                    }
                    if (foundSharedPart) {
                        continue;
                    }
                }

                // Process the nodes for the argument
                {
                    argument.processNodes(nodeMaker, isLast);

                    // Each node array represent a layer
                    final List<DeclareCommandsPacket.Node[]> nodesLayer = nodeMaker.getNodes();
                    storedArgumentsNodes.put(new IndexedArgument(syntax, argument, i), new ArrayList<>(nodesLayer));
                    for (int nodeIndex = lastArgumentNodeIndex; nodeIndex < nodesLayer.size(); nodeIndex++) {
                        final NodeMaker.ConfiguredNodes configuredNodes = nodeMaker.getConfiguredNodes().get(nodeIndex);
                        final NodeMaker.Options options = configuredNodes.getOptions();
                        final DeclareCommandsPacket.Node[] argumentNodes = nodesLayer.get(nodeIndex);

                        for (DeclareCommandsPacket.Node argumentNode : argumentNodes) {
                            final int childId = nodes.size();
                            nodeMaker.getNodeIdsMap().put(argumentNode, childId);
                            argChildren.add(childId);

                            // Enable ASK_SERVER suggestion if required
                            {
                                if (argument.hasSuggestion()) {
                                    argumentNode.flags |= 0x10; // Suggestion flag
                                    argumentNode.suggestionsType = SuggestionType.ASK_SERVER.getIdentifier();
                                }
                            }

                            // Append to the last node
                            {
                                final int[] children = ArrayUtils.toArray(argChildren);
                                for (DeclareCommandsPacket.Node lastNode : lastNodes) {
                                    lastNode.children = lastNode.children == null ?
                                            children :
                                            ArrayUtils.concatenateIntArrays(lastNode.children, children);
                                }
                            }

                            nodes.add(argumentNode);
                        }

                        if (options.shouldUpdateLastNode()) {
                            // 'previousNodes' used if the nodes options require to overwrite the parent
                            final DeclareCommandsPacket.Node[] previousNodes = options.getPreviousNodes();

                            lastNodes = previousNodes != null ? previousNodes : argumentNodes;
                            argChildren = new IntArrayList();
                        }
                    }

                    // Used to do not re-compute the previous arguments
                    lastArgumentNodeIndex = nodesLayer.size();
                }
            }

            nodeRequests.addAll(nodeMaker.getNodeRequests());

            syntaxesArguments.put(syntax, arguments);
        }

        storedArgumentsNodes.forEach((indexedArgument, argNodes) -> {
            int value = 0;
            for (DeclareCommandsPacket.Node[] n1 : argNodes) {
                for (DeclareCommandsPacket.Node n2 : n1) {
                    value = nodes.indexOf(n2);
                }
            }
            // FIXME: add syntax for indexing
            argumentIdentityMap.put(indexedArgument.argument, value);
        });

        literalNode.children = ArrayUtils.toArray(cmdChildren);
        return literalNode;
    }

    private @NotNull DeclareCommandsPacket.Node createMainNode(@NotNull String name, boolean executable) {
        DeclareCommandsPacket.Node literalNode = new DeclareCommandsPacket.Node();
        literalNode.flags = DeclareCommandsPacket.getFlag(DeclareCommandsPacket.NodeType.LITERAL, executable, false, false);
        literalNode.name = name;

        return literalNode;
    }

    private int addCommandNameNode(@NotNull DeclareCommandsPacket.Node commandNode,
                                   @NotNull IntList rootChildren,
                                   @NotNull List<DeclareCommandsPacket.Node> nodes) {
        final int node = nodes.size();
        rootChildren.add(node);
        nodes.add(commandNode);
        return node;
    }

    private static class IndexedArgument {
        private final CommandSyntax syntax;
        private final Argument<?> argument;
        private final int index;

        public IndexedArgument(CommandSyntax syntax, Argument<?> argument, int index) {
            this.syntax = syntax;
            this.argument = argument;
            this.index = index;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IndexedArgument that = (IndexedArgument) o;
            return index == that.index && Objects.equals(syntax, that.syntax) && Objects.equals(argument, that.argument);
        }

        @Override
        public int hashCode() {
            return Objects.hash(syntax, argument, index);
        }

        @Override
        public String toString() {
            return "IndexedArgument{" +
                    "syntax=" + syntax +
                    ", argument=" + argument +
                    ", index=" + index +
                    '}';
        }
    }
}
