package net.minestom.server.command;

import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

record NodeGraph(List<Node> nodes, Node root) {

    public Node resolveId(int id) {
        return nodes.get(id);
    }

    public List<Node> getChildren(Node node) {
        return node.children().intStream().mapToObj(this::resolveId).toList();
    }

    public @Nullable Node getRedirectTarget(Node node) {
        if (node.redirectTarget() == null) return null;
        return resolveId(node.redirectTarget().get());
    }

    @Contract("-> new")
    public DeclareCommandsPacket createPacket() {
        return new DeclareCommandsPacket(nodes.stream().map(Node::getPacketNode).toList(), root.id());
    }

    public String exportGarphvizDot() {
        final StringBuilder builder = new StringBuilder();
        final char statementSeparator = ';';
        builder.append("digraph G {");
        builder.append("rankdir=LR");
        builder.append(statementSeparator);
        for (Node node : nodes) {
            final AtomicInteger redirectTarget = node.redirectTarget();
            builder.append(node.id());
            builder.append(" [label=");
            builder.append(graphvizName(node));
            if (node.isRoot()) {
                builder.append(",shape=rectangle");
            }
            builder.append("]");
            builder.append(statementSeparator);
            if (node.children().isEmpty() && redirectTarget == null) continue;
            builder.append(node.id());
            builder.append(" -> { ");
            if (!node.children().isEmpty()) {
                builder.append(node.children().intStream().mapToObj(String::valueOf).collect(Collectors.joining(" ")));
                builder.append(" }");
                builder.append(statementSeparator);
            } else {
                builder.append(redirectTarget.get());
                builder.append(" } [style = dotted]");
                builder.append(statementSeparator);
            }
        }
        builder.append("}");
        return builder.toString();
    }

    private static String graphvizName(Node node) {
        return '"' + (node.isRoot() ? "root" : node.name()) + '"';
    }
}
