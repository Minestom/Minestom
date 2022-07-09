package net.minestom.server.command;

import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentLiteral;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

final class GraphConverter {
    private GraphConverter() {
        //no instance
    }

    @Contract("_ -> new")
    public static DeclareCommandsPacket createPacket(Graph graph) {
        List<DeclareCommandsPacket.Node> nodes = new ArrayList<>();
        final AtomicInteger idSource = new AtomicInteger(0);
        return new DeclareCommandsPacket(nodes, append(graph.root(), nodes, idSource));
    }

    private static int append(Graph.Node of, List<DeclareCommandsPacket.Node> to, AtomicInteger id) {
        final Argument<?> argument = of.argument();
        final List<Graph.Node> next = of.next();

        final DeclareCommandsPacket.Node node = new DeclareCommandsPacket.Node();
        int[] children = new int[next.size()];
        for (int i = 0; i < children.length; i++) children[i] = append(next.get(i), to, id);
        node.children = children;
        if (argument instanceof ArgumentLiteral literal) {
            if (literal.getId().isEmpty()) {
                node.flags = DeclareCommandsPacket.getFlag(DeclareCommandsPacket.NodeType.ROOT, false, false, false);
            } else {
                node.flags = DeclareCommandsPacket.getFlag(DeclareCommandsPacket.NodeType.LITERAL, false, false, false);
                node.name = argument.getId();
            }
        } else {
            node.flags = DeclareCommandsPacket.getFlag(DeclareCommandsPacket.NodeType.ARGUMENT, false, false, false);
            node.name = argument.getId();
            node.parser = argument.parser();
            node.properties = argument.nodeProperties();
        }
        to.add(node);
        return id.getAndIncrement();
    }
}
