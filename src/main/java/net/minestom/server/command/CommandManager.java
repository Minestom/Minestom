package net.minestom.server.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandDispatcher;
import net.minestom.server.command.builder.CommandSyntax;
import net.minestom.server.command.builder.arguments.*;
import net.minestom.server.command.builder.arguments.number.ArgumentDouble;
import net.minestom.server.command.builder.arguments.number.ArgumentFloat;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerCommandEvent;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.utils.ArrayUtils;
import net.minestom.server.utils.validate.Check;

import java.util.*;

public class CommandManager {

    private boolean running;
    private String commandPrefix = "/";

    private ConsoleSender consoleSender = new ConsoleSender();

    private CommandDispatcher<CommandSender> dispatcher = new CommandDispatcher<>();
    private Map<String, CommandProcessor> commandProcessorMap = new HashMap<>();

    public CommandManager() {
        running = true;
        // Setup console thread
        Thread consoleThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
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

    public void stopConsoleThread() {
        running = false;
    }

    public void register(Command<CommandSender> command) {
        this.dispatcher.register(command);
    }

    public void register(CommandProcessor commandProcessor) {
        this.commandProcessorMap.put(commandProcessor.getCommandName().toLowerCase(), commandProcessor);
    }

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
            this.dispatcher.execute(sender, command);
            return true;
        } catch (NullPointerException e) {
            String[] splitted = command.split(" ");
            String commandName = splitted[0];
            CommandProcessor commandProcessor = commandProcessorMap.get(commandName.toLowerCase());
            if (commandProcessor == null)
                return false;

            String[] args = command.substring(command.indexOf(" ") + 1).split(" ");

            return commandProcessor.process(sender, commandName, args);

        }
    }

    public String getCommandPrefix() {
        return commandPrefix;
    }

    public void setCommandPrefix(String commandPrefix) {
        this.commandPrefix = commandPrefix;
    }

    public ConsoleSender getConsoleSender() {
        return consoleSender;
    }

    public DeclareCommandsPacket createDeclareCommandsPacket(Player player) {
        return buildPacket2(player);
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

    private DeclareCommandsPacket buildPacket2(Player player) {
        DeclareCommandsPacket declareCommandsPacket = new DeclareCommandsPacket();

        List<DeclareCommandsPacket.Node> nodes = new ArrayList<>();
        ArrayList<Integer> rootChildren = new ArrayList<>();

        for (Command<CommandSender> command : dispatcher.getCommands()) {
            ArrayList<Integer> cmdChildren = new ArrayList<>();

            String name = command.getName();

            DeclareCommandsPacket.Node literalNode = new DeclareCommandsPacket.Node();
            literalNode.flags = 0b1; // literal
            literalNode.name = name;

            rootChildren.add(nodes.size());
            nodes.add(literalNode);

            for (CommandSyntax syntax : command.getSyntaxes()) {
                ArrayList<Integer> argChildren = cmdChildren;

                for (Argument argument : syntax.getArguments()) {

                    DeclareCommandsPacket.Node argumentNode = toNode(argument);

                    argChildren.add(nodes.size());
                    nodes.add(argumentNode);
                    System.out.println("size: " + argChildren.size());
                    argumentNode.children = ArrayUtils.toArray(argChildren);
                    argChildren = new ArrayList<>();
                }

            }
            System.out.println("test " + cmdChildren.size() + " : " + cmdChildren.get(0));
            literalNode.children = ArrayUtils.toArray(cmdChildren);

        }


        DeclareCommandsPacket.Node rootNode = new DeclareCommandsPacket.Node();
        rootNode.flags = 0;
        rootNode.children = ArrayUtils.toArray(rootChildren);

        nodes.add(rootNode);

        declareCommandsPacket.nodes = nodes.toArray(new DeclareCommandsPacket.Node[0]);
        declareCommandsPacket.rootIndex = nodes.size() - 1;

        return declareCommandsPacket;
    }

    private DeclareCommandsPacket.Node toNode(Argument argument) {
        DeclareCommandsPacket.Node argumentNode = new DeclareCommandsPacket.Node();
        argumentNode.flags = getFlag(NodeType.ARGUMENT, true, false, false);
        argumentNode.name = argument.getId();

        if (argument instanceof ArgumentBoolean) {
            argumentNode.parser = "brigadier:bool";
            argumentNode.properties = packetWriter -> packetWriter.writeByte((byte) 0);
        } else if (argument instanceof ArgumentDouble) {
            ArgumentDouble argumentDouble = (ArgumentDouble) argument;
            argumentNode.parser = "brigadier:double";
            argumentNode.properties = packetWriter -> {
                packetWriter.writeByte((byte) 0b11);
                packetWriter.writeDouble(argumentDouble.min);
                packetWriter.writeDouble(argumentDouble.max);
            };
        } else if (argument instanceof ArgumentFloat) {
            ArgumentFloat argumentFloat = (ArgumentFloat) argument;
            argumentNode.parser = "brigadier:float";
            argumentNode.properties = packetWriter -> {
                packetWriter.writeByte((byte) 0b11);
                packetWriter.writeFloat(argumentFloat.min);
                packetWriter.writeFloat(argumentFloat.max);
            };
        } else if (argument instanceof ArgumentInteger) {
            ArgumentInteger argumentInteger = (ArgumentInteger) argument;
            argumentNode.parser = "brigadier:integer";
            argumentNode.properties = packetWriter -> {
                packetWriter.writeByte((byte) 0b11);
                packetWriter.writeInt(argumentInteger.min);
                packetWriter.writeInt(argumentInteger.max);
            };
        } else if (argument instanceof ArgumentWord) {
            argumentNode.parser = "brigadier:string";
            argumentNode.properties = packetWriter -> {
                packetWriter.writeVarInt(0); // Single word
            };
        } else if (argument instanceof ArgumentString) {
            argumentNode.parser = "brigadier:string";
            argumentNode.properties = packetWriter -> {
                packetWriter.writeVarInt(1); // Quotable phrase
            };
        } else if (argument instanceof ArgumentStringArray) {
            argumentNode.parser = "brigadier:string";
            argumentNode.properties = packetWriter -> {
                packetWriter.writeVarInt(2); // Greedy phrase
            };
        }

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
            result |= 0x1;
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
