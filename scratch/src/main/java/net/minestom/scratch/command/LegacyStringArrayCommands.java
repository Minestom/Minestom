package net.minestom.scratch.command;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.client.play.ClientCommandChatPacket;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Simplest command system which only take a command name, and provide the arguments as a string.
 *
 * @param commands commands to register
 */
public record LegacyStringArrayCommands(Map<String, Consumer<String>> commands) {
    public LegacyStringArrayCommands {
        commands = Map.copyOf(commands);
    }

    public void consume(ClientCommandChatPacket packet) {
        final String message = packet.message();
        final String[] split = message.split(" ");
        final String command = split[0];
        final Consumer<String> consumer = commands.get(command);
        if (consumer != null) {
            // Remove the command from the message
            final String arguments = message.substring(command.length()).trim();
            consumer.accept(arguments);
        }
    }

    public DeclareCommandsPacket generatePacket() {
        List<DeclareCommandsPacket.Node> nodes = new ArrayList<>();
        DeclareCommandsPacket.Node root = new DeclareCommandsPacket.Node();
        root.flags = 0;
        nodes.add(root);

        IntArrayList rootChildren = new IntArrayList();

        for (String command : commands.keySet()) {
            {
                DeclareCommandsPacket.Node argumentNode = new DeclareCommandsPacket.Node();
                argumentNode.flags = 0x02; // argument
                argumentNode.flags |= 0x04; // executable
                argumentNode.name = "args";
                argumentNode.parser = "brigadier:string";
                argumentNode.properties = NetworkBuffer.makeArray(buffer -> buffer.write(NetworkBuffer.VAR_INT, 2));
                nodes.add(argumentNode);
            }
            {
                DeclareCommandsPacket.Node commandNode = new DeclareCommandsPacket.Node();
                commandNode.flags = 0x01; // literal
                commandNode.flags |= 0x04; // executable
                commandNode.name = command;
                commandNode.children = new int[]{nodes.size() - 1};
                rootChildren.add(nodes.size());
                nodes.add(commandNode);
            }
        }

        root.children = rootChildren.toIntArray();

        return new DeclareCommandsPacket(nodes, 0);
    }
}
