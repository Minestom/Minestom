package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandDispatcher;
import net.minestom.server.command.builder.CommandSyntax;
import net.minestom.server.command.builder.arguments.*;
import net.minestom.server.command.builder.arguments.minecraft.*;
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentEnchantment;
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentEntityType;
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentParticle;
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentPotion;
import net.minestom.server.command.builder.arguments.number.ArgumentDouble;
import net.minestom.server.command.builder.arguments.number.ArgumentFloat;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.command.builder.arguments.number.ArgumentNumber;
import net.minestom.server.command.builder.condition.CommandCondition;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerCommandEvent;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.utils.ArrayUtils;
import net.minestom.server.utils.validate.Check;

import java.util.*;
import java.util.function.Consumer;

public class CommandManager {

    private boolean running;
    private String commandPrefix = "/";

    private ConsoleSender consoleSender = new ConsoleSender();

    private CommandDispatcher dispatcher = new CommandDispatcher();
    private Map<String, CommandProcessor> commandProcessorMap = new HashMap<>();

    public CommandManager() {
        running = true;
        // Setup console thread
        Thread consoleThread = new Thread(() -> {
            final Scanner scanner = new Scanner(System.in);
            while (running) {
                if (scanner.hasNext()) {
                    String command = scanner.nextLine();
                    if (!command.startsWith(commandPrefix))
                        continue;
                    command = command.replaceFirst(commandPrefix, "");
                    execute(consoleSender, command);
                }
            }
        }, "ConsoleCommand-Thread");
        consoleThread.setDaemon(true);
        consoleThread.start();
    }

    /**
     * Stop the console responsive for the console commands processing
     * <p>
     * WARNING: it cannot be re-run later
     */
    public void stopConsoleThread() {
        running = false;
    }

    /**
     * Register a command with all the auto-completion features
     *
     * @param command the command to register
     */
    public void register(Command command) {
        this.dispatcher.register(command);
    }

    /**
     * Register a simple command without auto-completion
     *
     * @param commandProcessor the command to register
     */
    public void register(CommandProcessor commandProcessor) {
        this.commandProcessorMap.put(commandProcessor.getCommandName().toLowerCase(), commandProcessor);
    }

    /**
     * Execute a command for a sender
     *
     * @param sender  the sender of the command
     * @param command the raw command string (without the command prefix)
     * @return
     */
    public boolean execute(CommandSender sender, String command) {
        Check.notNull(sender, "Source cannot be null");
        Check.notNull(command, "Command string cannot be null");

        if (sender instanceof Player) {
            Player player = (Player) sender;

            PlayerCommandEvent playerCommandEvent = new PlayerCommandEvent(player, command);
            player.callEvent(PlayerCommandEvent.class, playerCommandEvent);

            if (playerCommandEvent.isCancelled())
                return false;

            command = playerCommandEvent.getCommand();
        }

        try {
            // Check for rich-command
            this.dispatcher.execute(sender, command);
            return true;
        } catch (NullPointerException e) {
            // Check for legacy-command
            final String[] splitted = command.split(" ");
            final String commandName = splitted[0];
            final CommandProcessor commandProcessor = commandProcessorMap.get(commandName.toLowerCase());
            if (commandProcessor == null)
                return false;

            // Execute the legacy-command
            final String[] args = command.substring(command.indexOf(" ") + 1).split(" ");

            return commandProcessor.process(sender, commandName, args);

        }
    }

    /**
     * Get the current command prefix (what should always be before the command name, ie: '/')
     *
     * @return the command prefix
     */
    public String getCommandPrefix() {
        return commandPrefix;
    }

    /**
     * Change the command prefix
     * <p>
     * This field can be changed half-way, but the client auto-completion still expect the '/' char
     *
     * @param commandPrefix the new command prefix
     */
    public void setCommandPrefix(String commandPrefix) {
        this.commandPrefix = commandPrefix;
    }

    /**
     * Get the console sender (which is used as a {@link CommandSender})
     *
     * @return the console sender
     */
    public ConsoleSender getConsoleSender() {
        return consoleSender;
    }

    /**
     * Get the declare commands packet for a specific player
     * <p>
     * Can be used to update the player auto-completion list
     *
     * @param player the player to get the commands packet
     * @return the {@link DeclareCommandsPacket} for {@code player}
     */
    public DeclareCommandsPacket createDeclareCommandsPacket(Player player) {
        return buildPacket(player);
    }

    /*private DeclareCommandsPacket buildPacket(Player player) {
        DeclareCommandsPacket declareCommandsPacket = new DeclareCommandsPacket();

        List<String> commands = new ArrayList<>();
        for (Command<CommandSender> command : dispatcher.getCommands()) {
            CommandCondition<Player> commandCondition = command.getCondition();
            if (commandCondition != null) {
                // Do not show command if return false
                if (!commandCondition.apply(player)) {
                    continue;
                }
            }
            commands.add(command.getName());
            for (String alias : command.getAliases()) {
                commands.add(alias);
            }
        }

        for (CommandProcessor commandProcessor : commandProcessorMap.values()) {
            // Do not show command if return false
            if (!commandProcessor.hasAccess(player))
                continue;

            commands.add(commandProcessor.getCommandName());
            String[] aliases = commandProcessor.getAliases();
            if (aliases == null || aliases.length == 0)
                continue;
            for (String alias : aliases) {
                commands.add(alias);
            }
        }


        List<DeclareCommandsPacket.Node> nodes = new ArrayList<>();
        ArrayList<Integer> rootChildren = new ArrayList<>();

        DeclareCommandsPacket.Node argNode = new DeclareCommandsPacket.Node();
        argNode.flags = 0b10;
        argNode.name = "arg";
        argNode.parser = "brigadier:string";
        argNode.properties = packetWriter -> {
            packetWriter.writeVarInt(0);
        };
        int argOffset = nodes.size();
        nodes.add(argNode);
        argNode.children = new int[]{argOffset};

        for (String commandName : commands) {

            DeclareCommandsPacket.Node literalNode = new DeclareCommandsPacket.Node();
            literalNode.flags = 0b1;
            literalNode.name = commandName;
            literalNode.children = new int[]{argOffset};

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
    }*/

    private DeclareCommandsPacket buildPacket(Player player) {
        DeclareCommandsPacket declareCommandsPacket = new DeclareCommandsPacket();

        List<DeclareCommandsPacket.Node> nodes = new ArrayList<>();
        // Contains the children of the main node (all commands name)
        ArrayList<Integer> rootChildren = new ArrayList<>();

        for (Command command : dispatcher.getCommands()) {
            // Check if player should see this command
            CommandCondition commandCondition = command.getCondition();
            if (commandCondition != null) {
                // Do not show command if return false
                if (!commandCondition.apply(player)) {
                    continue;
                }
            }

            // The main root of this command
            ArrayList<Integer> cmdChildren = new ArrayList<>();
            Collection<CommandSyntax> syntaxes = command.getSyntaxes();

            List<String> names = new ArrayList<>();
            names.add(command.getName());
            for (String alias : command.getAliases()) {
                names.add(alias);
            }
            for (String name : names) {
                createCommand(nodes, cmdChildren, name, syntaxes, rootChildren);
            }

        }

        final List<String> simpleCommands = new ArrayList<>();
        for (CommandProcessor commandProcessor : commandProcessorMap.values()) {
            // Do not show command if return false
            if (!commandProcessor.hasAccess(player))
                continue;

            simpleCommands.add(commandProcessor.getCommandName());
            String[] aliases = commandProcessor.getAliases();
            if (aliases == null || aliases.length == 0)
                continue;
            for (String alias : aliases) {
                simpleCommands.add(alias);
            }
        }

        for (String simpleCommand : simpleCommands) {
            // TODO server suggestion
            DeclareCommandsPacket.Node literalNode = new DeclareCommandsPacket.Node();
            literalNode.flags = getFlag(NodeType.LITERAL, true, false, false);
            literalNode.name = simpleCommand;
            literalNode.children = new int[0];
            //literalNode.suggestionsType = "minecraft:ask_server";

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

    private void createCommand(List<DeclareCommandsPacket.Node> nodes, ArrayList<Integer> cmdChildren, String name, Collection<CommandSyntax> syntaxes, ArrayList<Integer> rootChildren) {

        DeclareCommandsPacket.Node literalNode = createMainNode(name, syntaxes.isEmpty());

        rootChildren.add(nodes.size());
        nodes.add(literalNode);

        for (CommandSyntax syntax : syntaxes) {
            // Represent the last nodes computed in the last iteration
            List<DeclareCommandsPacket.Node> lastNodes = null;

            // Represent the children of the last node
            ArrayList<Integer> argChildren = null;

            final Argument[] arguments = syntax.getArguments();
            for (int i = 0; i < arguments.length; i++) {
                final Argument argument = arguments[i];
                final boolean isFirst = i == 0;
                final boolean isLast = i == arguments.length - 1;


                List<DeclareCommandsPacket.Node> argumentNodes = toNodes(argument, isLast);
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
                        lastNodes.forEach(n -> n.children = children);
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
                    argChildren = new ArrayList<>();
                    lastNodes = argumentNodes;
                }
            }

        }
        final int[] children = ArrayUtils.toArray(cmdChildren);
        //System.out.println("test " + children.length + " : " + children[0]);
        literalNode.children = children;
        if (children.length > 0) {
            literalNode.redirectedNode = children[0];
        }
    }

    private DeclareCommandsPacket.Node createMainNode(String name, boolean executable) {
        DeclareCommandsPacket.Node literalNode = new DeclareCommandsPacket.Node();
        literalNode.flags = getFlag(NodeType.LITERAL, executable, false, false);
        literalNode.name = name;

        return literalNode;
    }

    private List<DeclareCommandsPacket.Node> toNodes(Argument<?> argument, boolean executable) {
        List<DeclareCommandsPacket.Node> nodes = new ArrayList<>();

        /*DeclareCommandsPacket.Node testNode = simpleArgumentNode(nodes, argument, executable);
        testNode.parser = "minecraft:entity";
        testNode.properties = packetWriter -> packetWriter.writeByte((byte) 0x0);

        if (true) {
            return nodes;
        }*/

        if (argument instanceof ArgumentBoolean) {
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable);

            argumentNode.parser = "brigadier:bool";
            argumentNode.properties = packetWriter -> packetWriter.writeByte((byte) 0);
        } else if (argument instanceof ArgumentDouble) {
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable);

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
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable);

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
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable);

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
                DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable);
                wordConsumer.accept(argumentNode);
            }
        } else if (argument instanceof ArgumentString) {
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable);

            argumentNode.parser = "brigadier:string";
            argumentNode.properties = packetWriter -> {
                packetWriter.writeVarInt(1); // Quotable phrase
            };
        } else if (argument instanceof ArgumentStringArray) {
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable);

            argumentNode.parser = "brigadier:string";
            argumentNode.properties = packetWriter -> {
                packetWriter.writeVarInt(2); // Greedy phrase
            };
        } else if (argument instanceof ArgumentColor) {
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable);
            argumentNode.parser = "minecraft:color";
        } else if (argument instanceof ArgumentTime) {
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable);
            argumentNode.parser = "minecraft:time";
        } else if (argument instanceof ArgumentEnchantment) {
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable);
            argumentNode.parser = "minecraft:item_enchantment";
        } else if (argument instanceof ArgumentParticle) {
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable);
            argumentNode.parser = "minecraft:particle";
        } else if (argument instanceof ArgumentPotion) {
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable);
            argumentNode.parser = "minecraft:mob_effect";
        } else if (argument instanceof ArgumentEntityType) {
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable);
            argumentNode.parser = "minecraft:entity_summon";
        } else if (argument instanceof ArgumentIntRange) {
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable);
            argumentNode.parser = "minecraft:int_range";
        } else if (argument instanceof ArgumentFloatRange) {
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable);
            argumentNode.parser = "minecraft:float_range";
        } else if (argument instanceof ArgumentEntities) {
            ArgumentEntities argumentEntities = (ArgumentEntities) argument;
            DeclareCommandsPacket.Node argumentNode = simpleArgumentNode(nodes, argument, executable);
            argumentNode.parser = "minecraft:entity";
            argumentNode.properties = packetWriter -> {
                byte mask = 0;
                if (argumentEntities.isOnlySingleEntity()) {
                    mask += 1;
                }
                if (argumentEntities.isOnlyPlayers()) {
                    mask += 2;
                }
                packetWriter.writeByte(mask);
            };
        }

        return nodes;
    }

    private byte getNumberProperties(ArgumentNumber<? extends Number> argumentNumber) {
        byte result = 0;
        if (argumentNumber.hasMin())
            result += 1;
        if (argumentNumber.hasMax())
            result += 2;
        return result;
    }

    private DeclareCommandsPacket.Node simpleArgumentNode(List<DeclareCommandsPacket.Node> nodes,
                                                          Argument<?> argument, boolean executable) {
        DeclareCommandsPacket.Node argumentNode = new DeclareCommandsPacket.Node();
        nodes.add(argumentNode);

        argumentNode.flags = getFlag(NodeType.ARGUMENT, executable, false, false);
        argumentNode.name = argument.getId();

        return argumentNode;
    }

    private byte getFlag(NodeType type, boolean executable, boolean redirect, boolean suggestionType) {
        byte result = (byte) type.mask;

        if (executable) {
            result |= 0x4;
        }

        if (redirect) {
            result |= 0x8;
        }

        if (suggestionType) {
            result |= 0x10;
        }
        return result;
    }

    private enum NodeType {
        ROOT(0), LITERAL(0b1), ARGUMENT(0b10), NONE(0x11);

        private int mask;

        NodeType(int mask) {
            this.mask = mask;
        }

    }
}
