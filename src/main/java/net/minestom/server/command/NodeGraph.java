package net.minestom.server.command;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public record NodeGraph(List<Node> nodes, Node root) {
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
}
