package net.minestom.server.command;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandDispatcher;
import net.minestom.server.command.builder.CommandSyntax;
import net.minestom.server.command.builder.arguments.*;
import net.minestom.server.command.builder.arguments.minecraft.*;
import net.minestom.server.command.builder.arguments.minecraft.registry.*;
import net.minestom.server.command.builder.arguments.number.ArgumentDouble;
import net.minestom.server.command.builder.arguments.number.ArgumentFloat;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.command.builder.arguments.number.ArgumentNumber;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeBlockPosition;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeVec2;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeVec3;
import net.minestom.server.command.builder.condition.CommandCondition;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerCommandEvent;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.utils.ArrayUtils;
import net.minestom.server.utils.callback.CommandCallback;
import net.minestom.server.utils.validate.Check;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Consumer;

/**
 * Manager used to register {@link Command} and {@link CommandProcessor}.
 * <p>
 * It is also possible to simulate a command using {@link #execute(CommandSender, String)}.
 */
public final class CommandManager {

    public static final String COMMAND_PREFIX = "/";

    private volatile boolean running = true;

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
        for (String alias : command.getAliases()) {
            Check.stateCondition(commandExists(alias),
                    "A command with the name " + alias + " is already registered!");
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
     * @return true if the command hadn't been cancelled and has been successful
     */
    public boolean execute(@NotNull CommandSender sender, @NotNull String command) {

        // Command event
        if (sender instanceof Player) {
            Player player = (Player) sender;

            PlayerCommandEvent playerCommandEvent = new PlayerCommandEvent(player, command);
            player.callEvent(PlayerCommandEvent.class, playerCommandEvent);

            if (playerCommandEvent.isCancelled())
                return false;

            command = playerCommandEvent.getCommand();
        }

        // Process the command

        {
            // Check for rich-command
            final boolean result = this.dispatcher.execute(sender, command);
            if (result) {
                return true;
            } else {
                // Check for legacy-command
                final String[] splitCommand = command.split(StringUtils.SPACE);
                final String commandName = splitCommand[0];
                final CommandProcessor commandProcessor = commandProcessorMap.get(commandName.toLowerCase());
                if (commandProcessor == null) {
                    if (unknownCommandCallback != null) {
                        this.unknownCommandCallback.apply(sender, command);
                    }
                    return false;
                }

                // Execute the legacy-command
                final String[] args = command.substring(command.indexOf(StringUtils.SPACE) + 1).split(StringUtils.SPACE);

                return commandProcessor.process(sender, commandName, args);
            }
        }
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

        // Brigadier-like commands
        for (Command command : dispatcher.getCommands()) {
            // Check if player should see this command
            final CommandCondition commandCondition = command.getCondition();
            if (commandCondition != null) {
                // Do not show command if return false
                if (!commandCondition.canUse(player, null)) {
                    continue;
                }
            }

            // The main root of this command
            IntList cmdChildren = new IntArrayList();
            final Collection<CommandSyntax> syntaxes = command.getSyntaxes();

            List<String> names = new ArrayList<>();
            names.add(command.getName());
            names.addAll(Arrays.asList(command.getAliases()));
            for (String name : names) {
                createCommand(player, nodes, cmdChildren, name, syntaxes, rootChildren);
            }

        }

        // Pair<CommandName,EnabledTracking>
        final List<Pair<String, Boolean>> commandsPair = new ArrayList<>();
        for (CommandProcessor commandProcessor : commandProcessorMap.values()) {
            final boolean enableTracking = commandProcessor.enableWritingTracking();
            // Do not show command if return false
            if (!commandProcessor.hasAccess(player))
                continue;

            commandsPair.add(Pair.of(commandProcessor.getCommandName(), enableTracking));
            final String[] aliases = commandProcessor.getAliases();
            if (aliases == null || aliases.length == 0)
                continue;
            for (String alias : aliases) {
                commandsPair.add(Pair.of(alias, enableTracking));
            }
        }

        for (Pair<String, Boolean> pair : commandsPair) {
            final String name = pair.getLeft();
            final boolean tracking = pair.getRight();
            // Server suggestion (ask_server)
            {
                DeclareCommandsPacket.Node tabNode = new DeclareCommandsPacket.Node();
                tabNode.flags = getFlag(NodeType.ARGUMENT, true, false, tracking);
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
            literalNode.flags = getFlag(NodeType.LITERAL, true, false, false);
            literalNode.name = name;
            literalNode.children = new int[]{nodes.size() - 1};

            rootChildren.add(nodes.size());
            nodes.add(literalNode);
        }

        DeclareCommandsPacket.Node rootNode = new DeclareCommandsPacket.Node();
        rootNode.flags = 0;
        rootNode.children = ArrayUtils.toArray(rootChildren);

        nodes.add(rootNode);

        declareCommandsPacket.nodes = nodes.toArray(new DeclareCommandsPacket.Node[0]);
        declareCommandsPacket.rootIndex = nodes.size() - 1;

        return declareCommandsPacket;
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
     */
    private void createCommand(@NotNull CommandSender sender,
                               @NotNull List<DeclareCommandsPacket.Node> nodes,
                               @NotNull IntList cmdChildren,
                               @NotNull String name,
                               @NotNull Collection<CommandSyntax> syntaxes,
                               @NotNull IntList rootChildren) {

        DeclareCommandsPacket.Node literalNode = createMainNode(name, syntaxes.isEmpty());

        rootChildren.add(nodes.size());
        nodes.add(literalNode);

        // Contains the arguments of the already-parsed syntaxes
        List<Argument<?>[]> syntaxesArguments = new ArrayList<>();
        // Contains the nodes of an argument
        Map<Argument<?>, List<DeclareCommandsPacket.Node>> storedArgumentsNodes = new HashMap<>();

        for (CommandSyntax syntax : syntaxes) {
            final CommandCondition commandCondition = syntax.getCommandCondition();
            if (commandCondition != null && !commandCondition.canUse(sender, null)) {
                // Sender does not have the right to use this syntax, ignore it
                continue;
            }


            // Represent the last nodes computed in the last iteration
            List<DeclareCommandsPacket.Node> lastNodes = null;

            // Represent the children of the last node
            IntList argChildren = null;

            final Argument<?>[] arguments = syntax.getArguments();
            for (int i = 0; i < arguments.length; i++) {
                final Argument<?> argument = arguments[i];
                final boolean isFirst = i == 0;
                final boolean isLast = i == arguments.length - 1;

                // Find shared part
                boolean foundSharedPart = false;
                for (Argument<?>[] parsedArguments : syntaxesArguments) {
                    if (ArrayUtils.sameStart(arguments, parsedArguments, i + 1)) {
                        final Argument<?> sharedArgument = parsedArguments[i];

                        argChildren = new IntArrayList();
                        lastNodes = storedArgumentsNodes.get(sharedArgument);
                        foundSharedPart = true;
                    }
                }
                if (foundSharedPart) {
                    continue;
                }


                final List<DeclareCommandsPacket.Node> argumentNodes = toNodes(argument, isLast);
                storedArgumentsNodes.put(argument, argumentNodes);
                for (DeclareCommandsPacket.Node node : argumentNodes) {
                    final int childId = nodes.size();

                    if (isFirst) {
                        // Add to main command child
                        cmdChildren.add(childId);
                    } else {
                        // Add to previous argument children
                        argChildren.add(childId);
                    }

                    if (lastNodes != null) {
                        final int[] children = ArrayUtils.toArray(argChildren);
                        lastNodes.forEach(n -> n.children = n.children == null ?
                                children :
                                ArrayUtils.concatenateIntArrays(n.children, children));
                    }

                    nodes.add(node);
                }

                //System.out.println("debug: " + argument.getId() + " : " + isFirst + " : " + isLast);
                //System.out.println("debug2: " + i);
                //System.out.println("size: " + (argChildren != null ? argChildren.size() : "NULL"));

                if (isLast) {
                    // Last argument doesn't have children
                    final int[] children = new int[0];
                    argumentNodes.forEach(node -> node.children = children);
                } else {
                    // Create children list which will be filled during next iteration
                    argChildren = new IntArrayList();
                    lastNodes = argumentNodes;
                }
            }

            syntaxesArguments.add(arguments);

        }
        final int[] children = ArrayUtils.toArray(cmdChildren);
        //System.out.println("test " + children.length + " : " + children[0]);
        literalNode.children = children;
        if (children.length > 0) {
            literalNode.redirectedNode = children[0];
        }
    }

    @NotNull
    private DeclareCommandsPacket.Node createMainNode(@NotNull String name, boolean executable) {
        DeclareCommandsPacket.Node literalNode = new DeclareCommandsPacket.Node();
        literalNode.flags = getFlag(NodeType.LITERAL, executable, false, false);
        literalNode.name = name;

        return literalNode;
    }

    /**
     * Converts an argument to a node with the correct brigadier parser.
     *
     * @param argument   the argument to convert
     * @param executable true if this is the last argument, false otherwise
     * @return the list of nodes that the argument require
     */
    @NotNull
    private List<DeclareCommandsPacket.Node> toNodes(@NotNull Argument<?> argument, boolean executable) {
        List<DeclareCommandsPacket.Node> nodes = new ArrayList<>();

        // You can uncomment this to test any brigadier parser on the client
        /*DeclareCommandsPacket.Node testNode = simpleArgumentNode(nodes, argument, executable, false);
        testNode.parser = "minecraft:block_state";

        if (true) {
            return nodes;
        }*/

        if (argument instanceof ArgumentBoolean) {
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable, false);

            argumentNode.parser = "brigadier:bool";
        } else if (argument instanceof ArgumentDouble) {
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable, false);

            ArgumentDouble argumentDouble = (ArgumentDouble) argument;
            argumentNode.parser = "brigadier:double";
            argumentNode.properties = packetWriter -> {
                packetWriter.writeByte(getNumberProperties(argumentDouble));
                if (argumentDouble.hasMin())
                    packetWriter.writeDouble(argumentDouble.getMin());
                if (argumentDouble.hasMax())
                    packetWriter.writeDouble(argumentDouble.getMax());
            };
        } else if (argument instanceof ArgumentFloat) {
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable, false);

            ArgumentFloat argumentFloat = (ArgumentFloat) argument;
            argumentNode.parser = "brigadier:float";
            argumentNode.properties = packetWriter -> {
                packetWriter.writeByte(getNumberProperties(argumentFloat));
                if (argumentFloat.hasMin())
                    packetWriter.writeFloat(argumentFloat.getMin());
                if (argumentFloat.hasMax())
                    packetWriter.writeFloat(argumentFloat.getMax());
            };
        } else if (argument instanceof ArgumentInteger) {
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable, false);

            ArgumentInteger argumentInteger = (ArgumentInteger) argument;
            argumentNode.parser = "brigadier:integer";
            argumentNode.properties = packetWriter -> {
                packetWriter.writeByte(getNumberProperties(argumentInteger));
                if (argumentInteger.hasMin())
                    packetWriter.writeInt(argumentInteger.getMin());
                if (argumentInteger.hasMax())
                    packetWriter.writeInt(argumentInteger.getMax());
            };
        } else if (argument instanceof ArgumentWord) {

            ArgumentWord argumentWord = (ArgumentWord) argument;

            // Add the single word properties + parser
            final Consumer<DeclareCommandsPacket.Node> wordConsumer = node -> {
                node.parser = "brigadier:string";
                node.properties = packetWriter -> {
                    packetWriter.writeVarInt(0); // Single word
                };
            };

            final boolean hasRestriction = argumentWord.hasRestrictions();
            if (hasRestriction) {
                // Create a node for each restrictions as literal
                for (String restrictionWord : argumentWord.getRestrictions()) {
                    DeclareCommandsPacket.Node argumentNode = new DeclareCommandsPacket.Node();
                    nodes.add(argumentNode);

                    argumentNode.flags = getFlag(NodeType.LITERAL, executable, false, false);
                    argumentNode.name = restrictionWord;
                    wordConsumer.accept(argumentNode);
                }
            } else {
                // Can be any word, add only one argument node
                DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable, false);
                wordConsumer.accept(argumentNode);
            }
        } else if (argument instanceof ArgumentDynamicWord) {
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable, true);

            final SuggestionType suggestionType = ((ArgumentDynamicWord) argument).getSuggestionType();

            argumentNode.parser = "brigadier:string";
            argumentNode.properties = packetWriter -> {
                packetWriter.writeVarInt(0); // Single word
            };
            argumentNode.suggestionsType = suggestionType.getIdentifier();

        } else if (argument instanceof ArgumentString) {
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable, false);

            argumentNode.parser = "brigadier:string";
            argumentNode.properties = packetWriter -> {
                packetWriter.writeVarInt(1); // Quotable phrase
            };
        } else if (argument instanceof ArgumentStringArray) {
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable, false);

            argumentNode.parser = "brigadier:string";
            argumentNode.properties = packetWriter -> {
                packetWriter.writeVarInt(2); // Greedy phrase
            };
        } else if (argument instanceof ArgumentDynamicStringArray) {
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable, true);

            argumentNode.parser = "brigadier:string";
            argumentNode.properties = packetWriter -> {
                packetWriter.writeVarInt(2); // Greedy phrase
            };
            argumentNode.suggestionsType = "minecraft:ask_server";
        } else if (argument instanceof ArgumentColor) {
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable, false);
            argumentNode.parser = "minecraft:color";
        } else if (argument instanceof ArgumentTime) {
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable, false);
            argumentNode.parser = "minecraft:time";
        } else if (argument instanceof ArgumentEnchantment) {
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable, false);
            argumentNode.parser = "minecraft:item_enchantment";
        } else if (argument instanceof ArgumentParticle) {
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable, false);
            argumentNode.parser = "minecraft:particle";
        } else if (argument instanceof ArgumentPotionEffect) {
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable, false);
            argumentNode.parser = "minecraft:mob_effect";
        } else if (argument instanceof ArgumentEntityType) {
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable, false);
            argumentNode.parser = "minecraft:entity_summon";
        } else if (argument instanceof ArgumentBlockState) {
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable, false);
            argumentNode.parser = "minecraft:block_state";
        } else if (argument instanceof ArgumentIntRange) {
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable, false);
            argumentNode.parser = "minecraft:int_range";
        } else if (argument instanceof ArgumentFloatRange) {
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable, false);
            argumentNode.parser = "minecraft:float_range";
        } else if (argument instanceof ArgumentEntity) {
            ArgumentEntity argumentEntity = (ArgumentEntity) argument;
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable, false);
            argumentNode.parser = "minecraft:entity";
            argumentNode.properties = packetWriter -> {
                byte mask = 0;
                if (argumentEntity.isOnlySingleEntity()) {
                    mask += 1;
                }
                if (argumentEntity.isOnlyPlayers()) {
                    mask += 2;
                }
                packetWriter.writeByte(mask);
            };
        } else if (argument instanceof ArgumentItemStack) {
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable, false);
            argumentNode.parser = "minecraft:item_stack";
        } else if (argument instanceof ArgumentNbtCompoundTag) {
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable, false);
            argumentNode.parser = "minecraft:nbt_compound_tag";
        } else if (argument instanceof ArgumentNbtTag) {
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable, false);
            argumentNode.parser = "minecraft:nbt_tag";
        } else if (argument instanceof ArgumentRelativeBlockPosition) {
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable, false);
            argumentNode.parser = "minecraft:block_pos";
        } else if (argument instanceof ArgumentRelativeVec3) {
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable, false);
            argumentNode.parser = "minecraft:vec3";
        } else if (argument instanceof ArgumentRelativeVec2) {
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable, false);
            argumentNode.parser = "minecraft:vec2";
        }

        return nodes;
    }

    private byte getNumberProperties(@NotNull ArgumentNumber<? extends Number> argumentNumber) {
        byte result = 0;
        if (argumentNumber.hasMin())
            result += 1;
        if (argumentNumber.hasMax())
            result += 2;
        return result;
    }

    /**
     * Builds an argument nod and add it to the nodes list.
     *
     * @param nodes      the current nodes list
     * @param argument   the argument
     * @param executable true if this will be the last argument, false otherwise
     * @return the created {@link DeclareCommandsPacket.Node}
     */
    @NotNull
    private DeclareCommandsPacket.Node simpleArgumentNode(@NotNull List<DeclareCommandsPacket.Node> nodes,
                                                          @NotNull Argument<?> argument, boolean executable, boolean suggestion) {
        DeclareCommandsPacket.Node argumentNode = new DeclareCommandsPacket.Node();
        nodes.add(argumentNode);

        argumentNode.flags = getFlag(NodeType.ARGUMENT, executable, false, suggestion);
        argumentNode.name = argument.getId();

        return argumentNode;
    }

    private byte getFlag(@NotNull NodeType type, boolean executable, boolean redirect, boolean suggestionType) {
        byte result = (byte) type.mask;

        if (executable) {
            result |= 0x04;
        }

        if (redirect) {
            result |= 0x08;
        }

        if (suggestionType) {
            result |= 0x10;
        }
        return result;
    }

    private enum NodeType {
        ROOT(0), LITERAL(0b1), ARGUMENT(0b10), NONE(0x11);

        private final int mask;

        NodeType(int mask) {
            this.mask = mask;
        }

    }
}
