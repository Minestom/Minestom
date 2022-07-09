package net.minestom.server.command;

import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentLiteral;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.Contract;

final class GraphConverter {
    private GraphConverter() {
        //no instance
    }

    public static DeclareCommandsPacket.Node getPacketNode(Node node) {
        final DeclareCommandsPacket.Node packetNode = new DeclareCommandsPacket.Node();
        packetNode.children = node.children().toIntArray();
        final Argument<?> arg = node.arg();
        DeclareCommandsPacket.NodeType type = arg == null ? DeclareCommandsPacket.NodeType.ROOT :
                arg instanceof ArgumentLiteral ? DeclareCommandsPacket.NodeType.LITERAL :
                        DeclareCommandsPacket.NodeType.ARGUMENT;
        packetNode.flags = DeclareCommandsPacket.getFlag(type, node.executable(), node.redirectTarget() != null,
                type == DeclareCommandsPacket.NodeType.ARGUMENT && arg.hasSuggestion());
        packetNode.name = arg == null ? null : arg.getId();
        if (node.redirectTarget() != null) {
            packetNode.redirectedNode = node.redirectTarget().get();
        }
        if (type == DeclareCommandsPacket.NodeType.ARGUMENT) {
            packetNode.properties = arg.nodeProperties();
            packetNode.parser = arg.parser();
            if (arg.hasSuggestion()) {
                //noinspection ConstantConditions
                packetNode.suggestionsType = arg.suggestionType().getIdentifier();
            }
        }
        return packetNode;
    }

    @Contract("_ -> new")
    public static DeclareCommandsPacket createPacket(NodeGraph graph) {
        return new DeclareCommandsPacket(graph.nodes().stream().map(GraphConverter::getPacketNode).toList(), graph.root().id());
    }
}
