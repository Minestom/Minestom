package net.minestom.server.command;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentLiteral;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket.NodeType;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

record Node(int id, IntList children, NodeType type, String name, boolean executable, Argument<?> arg,
            AtomicInteger redirectTarget) {

    public static Node root(int id) {
        return new Node(id, new IntArrayList(), NodeType.ROOT, null, false, null, null);
    }
    public static Node literal(int id, String name, boolean executable, @Nullable AtomicInteger redirectTarget) {
        return new Node(id, new IntArrayList(), NodeType.LITERAL, name, executable, new ArgumentLiteral(name), redirectTarget);
    }

    public static Node argument(int id, Argument<?> argument, boolean executable, @Nullable AtomicInteger redirectTarget) {
        return new Node(id, new IntArrayList(), NodeType.ARGUMENT, argument.getId(), executable, argument, redirectTarget);
    }

    public void addChild(Node ...nodes) {
        for (Node node : nodes) {
            children.add(node.id);
        }
    }

    public boolean isParentOf(Node node) {
        return children.contains(node.id());
    }

    public boolean isRoot() {
        return type == NodeType.ROOT;
    }

    public DeclareCommandsPacket.Node getPacketNode() {
        final DeclareCommandsPacket.Node node = new DeclareCommandsPacket.Node();
        node.children = children.toIntArray();
        node.flags = DeclareCommandsPacket.getFlag(type, executable, redirectTarget != null,
                type == NodeType.ARGUMENT && arg.hasSuggestion());
        node.name = name;
        if (redirectTarget != null) {
            node.redirectedNode = redirectTarget.get();
        }
        if (type == NodeType.ARGUMENT) {
            node.properties = arg.nodeProperties();
            node.parser = arg.parser();
            if (arg.hasSuggestion()) {
                //noinspection ConstantConditions
                node.suggestionsType = arg.suggestionType().getIdentifier();
            }
        }
        return node;
    }
}
