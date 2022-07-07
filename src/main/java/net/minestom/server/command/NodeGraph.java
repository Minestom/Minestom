package net.minestom.server.command;

import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
}
