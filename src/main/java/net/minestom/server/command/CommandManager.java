package net.minestom.server.command;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.*;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.condition.CommandCondition;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerCommandEvent;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.utils.ArrayUtils;
import net.minestom.server.utils.callback.CommandCallback;
import net.minestom.server.utils.validate.Check;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manager used to register {@link Command} and {@link CommandProcessor}.
 * <p>
 * It is also possible to simulate a command using {@link #execute(CommandSender, String)}.
 */
public final class CommandManager {

    public static final String COMMAND_PREFIX = "/";

    private volatile boolean running = true;

    private final ServerSender serverSender = new ServerSender();
    private final ConsoleSender consoleSender = new ConsoleSender();

    private final CommandDispatcher dispatcher = new CommandDispatcher();
    private final Map<String, CommandProcessor> commandProcessorMap = new HashMap<>();

    private CommandCallback unknownCommandCallback;

    public CommandManager() {
    }

    /**
     * Stops the console responsible for the console commands processing.
     * <p>
     * WARNING: it cannot be re-run later.
     */
    public void stopConsoleThread() {
        running = false;
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
    @Nullable
    public Command getCommand(@NotNull String commandName) {
        return dispatcher.findCommand(commandName);
    }

    /**
     * Registers a {@link CommandProcessor}.
     *
     * @param commandProcessor the command to register
     * @throws IllegalStateException if a command with the same name already exists
     */
    public synchronized void register(@NotNull CommandProcessor commandProcessor) {
        final String commandName = commandProcessor.getCommandName().toLowerCase();
        Check.stateCondition(commandExists(commandName),
                "A command with the name " + commandName + " is already registered!");
        this.commandProcessorMap.put(commandName, commandProcessor);
        // Register aliases
        final String[] aliases = commandProcessor.getAliases();
        if (aliases != null && aliases.length > 0) {
            for (String alias : aliases) {
                Check.stateCondition(commandExists(alias),
                        "A command with the name " + alias + " is already registered!");

                this.commandProcessorMap.put(alias.toLowerCase(), commandProcessor);
            }
        }
    }

    /**
     * Gets the {@link CommandProcessor} registered by {@link #register(CommandProcessor)}.
     *
     * @param commandName the command name
     * @return the command associated with the name, null if not any
     */
    @Nullable
    public CommandProcessor getCommandProcessor(@NotNull String commandName) {
        return commandProcessorMap.get(commandName.toLowerCase());
    }

    /**
     * Gets if a command with the name {@code commandName} already exists or name.
     *
     * @param commandName the command name to check
     * @return true if the command does exist
     */
    public boolean commandExists(@NotNull String commandName) {
        commandName = commandName.toLowerCase();
        return dispatcher.findCommand(commandName) != null ||
                commandProcessorMap.get(commandName) != null;
    }

    /**
     * Executes a command for a {@link ConsoleSender}.
     *
     * @param sender  the sender of the command
     * @param command the raw command string (without the command prefix)
     * @return the execution result
     */
    @NotNull
    public CommandResult execute(@NotNull CommandSender sender, @NotNull String command) {

        // Command event
        if (sender instanceof Player) {
            Player player = (Player) sender;

            PlayerCommandEvent playerCommandEvent = new PlayerCommandEvent(player, command);
            player.callEvent(PlayerCommandEvent.class, playerCommandEvent);

            if (playerCommandEvent.isCancelled())
                return CommandResult.of(CommandResult.Type.CANCELLED, command);

            command = playerCommandEvent.getCommand();
        }

        // Process the command

        {
            // Check for rich-command
            final CommandResult result = this.dispatcher.execute(sender, command);
            if (result.getType() != CommandResult.Type.UNKNOWN) {
                return result;
            } else {
                // Check for legacy-command
                final String[] splitCommand = command.split(StringUtils.SPACE);
                final String commandName = splitCommand[0];
                final CommandProcessor commandProcessor = commandProcessorMap.get(commandName.toLowerCase());
                if (commandProcessor == null) {
                    if (unknownCommandCallback != null) {
                        this.unknownCommandCallback.apply(sender, command);
                    }
                    return CommandResult.of(CommandResult.Type.UNKNOWN, command);
                }

                // Execute the legacy-command
                final String[] args = command.substring(command.indexOf(StringUtils.SPACE) + 1).split(StringUtils.SPACE);
                commandProcessor.process(sender, commandName, args);
                return CommandResult.of(CommandResult.Type.SUCCESS, command);
            }
        }
    }

    /**
     * Executes the command using a {@link ServerSender} to do not
     * print the command messages, and rely instead on the command return data.
     *
     * @see #execute(CommandSender, String)
     */
    @NotNull
    public CommandResult executeServerCommand(@NotNull String command) {
        return execute(serverSender, command);
    }

    @NotNull
    public CommandDispatcher getDispatcher() {
        return dispatcher;
    }

    /**
     * Gets the callback executed once an unknown command is run.
     *
     * @return the unknown command callback, null if not any
     */
    @Nullable
    public CommandCallback getUnknownCommandCallback() {
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
    @NotNull
    public ConsoleSender getConsoleSender() {
        return consoleSender;
    }

    /**
     * Starts the thread responsible for executing commands from the console.
     */
    public void startConsoleThread() {
        Thread consoleThread = new Thread(() -> {
            BufferedReader bi = new BufferedReader(new InputStreamReader(System.in));
            while (running) {

                try {

                    if (bi.ready()) {
                        final String command = bi.readLine();
                        execute(consoleSender, command);
                    }
                } catch (IOException e) {
                    MinecraftServer.getExceptionManager().handleException(e);
                    continue;
                }

                // Prevent permanent looping
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    MinecraftServer.getExceptionManager().handleException(e);
                }

            }
            try {
                bi.close();
            } catch (IOException e) {
                MinecraftServer.getExceptionManager().handleException(e);
            }
        }, "ConsoleCommand-Thread");
        consoleThread.setDaemon(true);
        consoleThread.start();
    }

    /**
     * Gets the {@link DeclareCommandsPacket} for a specific player.
     * <p>
     * Can be used to update a player auto-completion list.
     *
     * @param player the player to get the commands packet
     * @return the {@link DeclareCommandsPacket} for {@code player}
     */
    @NotNull
    public DeclareCommandsPacket createDeclareCommandsPacket(@NotNull Player player) {
        return buildPacket(player);
    }

    /**
     * Builds the {@link DeclareCommandsPacket} for a {@link Player}.
     *
     * @param player the player to build the packet for
     * @return the commands packet for the specific player
     */
    @NotNull
    private DeclareCommandsPacket buildPacket(@NotNull Player player) {
        DeclareCommandsPacket declareCommandsPacket = new DeclareCommandsPacket();

        List<DeclareCommandsPacket.Node> nodes = new ArrayList<>();
        // Contains the children of the main node (all commands name)
        IntList rootChildren = new IntArrayList();

        // Root node
        DeclareCommandsPacket.Node rootNode = new DeclareCommandsPacket.Node();
        rootNode.flags = 0;
        nodes.add(rootNode);

        // Brigadier-like commands
        for (Command command : dispatcher.getCommands()) {
            serializeCommand(player, command, nodes, rootChildren);
        }

        // Pair<CommandName,EnabledTracking>
        final Object2BooleanMap<String> commandsPair = new Object2BooleanOpenHashMap<>();
        for (CommandProcessor commandProcessor : commandProcessorMap.values()) {
            final boolean enableTracking = commandProcessor.enableWritingTracking();
            // Do not show command if return false
            if (!commandProcessor.hasAccess(player))
                continue;

            commandsPair.put(commandProcessor.getCommandName(), enableTracking);
            final String[] aliases = commandProcessor.getAliases();
            if (aliases == null || aliases.length == 0)
                continue;
            for (String alias : aliases) {
                commandsPair.put(alias, enableTracking);
            }
        }

        for (Object2BooleanMap.Entry<String> entry : commandsPair.object2BooleanEntrySet()) {
            final String name = entry.getKey();
            final boolean tracking = entry.getBooleanValue();
            // Server suggestion (ask_server)
            {
                DeclareCommandsPacket.Node tabNode = new DeclareCommandsPacket.Node();
                tabNode.flags = DeclareCommandsPacket.getFlag(DeclareCommandsPacket.NodeType.ARGUMENT,
                        true, false, tracking);
                tabNode.name = tracking ? "tab_completion" : "args";
                tabNode.parser = "brigadier:string";
                tabNode.properties = packetWriter -> packetWriter.writeVarInt(2); // Greedy phrase
                tabNode.children = new int[0];
                if (tracking) {
                    tabNode.suggestionsType = "minecraft:ask_server";
                }

                nodes.add(tabNode);
            }

            DeclareCommandsPacket.Node literalNode = new DeclareCommandsPacket.Node();
            literalNode.flags = DeclareCommandsPacket.getFlag(DeclareCommandsPacket.NodeType.LITERAL,
                    true, false, false);
            literalNode.name = name;
            literalNode.children = new int[]{nodes.size() - 1};

            addCommandNameNode(literalNode, rootChildren, nodes);
        }

        // Add root node children
        rootNode.children = ArrayUtils.toArray(rootChildren);

        declareCommandsPacket.nodes = nodes.toArray(new DeclareCommandsPacket.Node[0]);
        declareCommandsPacket.rootIndex = 0;

        return declareCommandsPacket;
    }

    private int serializeCommand(CommandSender sender, Command command,
                                 List<DeclareCommandsPacket.Node> nodes,
                                 IntList rootChildren) {
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
        final DeclareCommandsPacket.Node mainNode = createCommand(sender, nodes, cmdChildren,
                command.getName(), syntaxes, rootChildren);
        final int mainNodeIndex = nodes.indexOf(mainNode);

        // Serialize all the subcommands
        for (Command subcommand : command.getSubcommands()) {
            final int subNodeIndex = serializeCommand(sender, subcommand, nodes, cmdChildren);
            if (subNodeIndex != -1) {
                mainNode.children = ArrayUtils.concatenateIntArrays(mainNode.children, new int[]{subNodeIndex});
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
    private DeclareCommandsPacket.Node createCommand(@NotNull CommandSender sender,
                                                     @NotNull List<DeclareCommandsPacket.Node> nodes,
                                                     @NotNull IntList cmdChildren,
                                                     @NotNull String name,
                                                     @NotNull Collection<CommandSyntax> syntaxes,
                                                     @NotNull IntList rootChildren) {

        DeclareCommandsPacket.Node literalNode = createMainNode(name, syntaxes.isEmpty());

        final int literalNodeId = addCommandNameNode(literalNode, rootChildren, nodes);

        // Contains the arguments of the already-parsed syntaxes
        List<Argument<?>[]> syntaxesArguments = new ArrayList<>();
        // Contains the nodes of an argument
        Map<Argument<?>, List<DeclareCommandsPacket.Node[]>> storedArgumentsNodes = new HashMap<>();

        // Sort syntaxes by argument count. Brigadier requires it.
        syntaxes = syntaxes.stream().sorted(Comparator.comparingInt(o -> -o.getArguments().length)).collect(Collectors.toList());
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

                // Search previously parsed syntaxes to find identical part in order to create a node between those
                {
                    // Find shared part
                    boolean foundSharedPart = false;
                    for (Argument<?>[] parsedArguments : syntaxesArguments) {
                        final int index = i + 1;
                        if (ArrayUtils.sameStart(arguments, parsedArguments, index)) {
                            final Argument<?> sharedArgument = parsedArguments[i];
                            final List<DeclareCommandsPacket.Node[]> storedNodes = storedArgumentsNodes.get(sharedArgument);

                            argChildren = new IntArrayList();
                            lastNodes = storedNodes.get(index);
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
                    storedArgumentsNodes.put(argument, nodesLayer);
                    for (int nodeIndex = lastArgumentNodeIndex; nodeIndex < nodesLayer.size(); nodeIndex++) {
                        final NodeMaker.ConfiguredNodes configuredNodes = nodeMaker.getConfiguredNodes().get(nodeIndex);
                        final NodeMaker.Options options = configuredNodes.getOptions();
                        final DeclareCommandsPacket.Node[] argumentNodes = nodesLayer.get(nodeIndex);

                        for (DeclareCommandsPacket.Node argumentNode : argumentNodes) {
                            final int childId = nodes.size();
                            nodeMaker.getNodeIdsMap().put(argumentNode, childId);
                            argChildren.add(childId);

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
            syntaxesArguments.add(arguments);
        }

        literalNode.children = ArrayUtils.toArray(cmdChildren);
        return literalNode;

    }

    @NotNull
    private DeclareCommandsPacket.Node createMainNode(@NotNull String name, boolean executable) {
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
}
