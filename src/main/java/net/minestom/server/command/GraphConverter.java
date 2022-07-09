package net.minestom.server.command;

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
        final DeclareCommandsPacket.Node node = new DeclareCommandsPacket.Node();
        node.children = of.next().stream().mapToInt(x -> append(x, to, id)).toArray();
        if (of.argument() instanceof ArgumentLiteral literal) {
            if (literal.getId().isEmpty()) {
                node.flags = DeclareCommandsPacket.getFlag(DeclareCommandsPacket.NodeType.ROOT, false, false, false);
            } else {
                node.flags = DeclareCommandsPacket.getFlag(DeclareCommandsPacket.NodeType.LITERAL, false, false, false);
                node.name = of.argument().getId();
            }
        } else {
            node.flags = DeclareCommandsPacket.getFlag(DeclareCommandsPacket.NodeType.ARGUMENT, false, false, false);
            node.name = of.argument().getId();
            node.parser = of.argument().parser();
            node.properties = of.argument().nodeProperties();
        }
        to.add(node);
        return id.getAndIncrement();
    }
}
