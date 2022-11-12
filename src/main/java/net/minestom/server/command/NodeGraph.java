package net.minestom.server.command;

import it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

class NodeGraph {
    private final ObjectList<Node> nodes;
    private final Node root;

    NodeGraph(ObjectSet<Node> nodes, int rootId) {
        this.nodes = new ObjectImmutableList<>(nodes.stream().sorted(Comparator.comparing(Node::id)).toList());
        this.root = this.nodes.get(rootId);
        assert root.isRoot() : "rootId doesn't point to the root node";
        assert this.nodes.stream().filter(Node::isRoot).count() == 1 : "Invalid root node count!";
    }

    public Node resolveId(int id) {
        return nodes.get(id);
    }

    public List<Node> getChildren(Node node) {
        return node.children().intStream().mapToObj(this::resolveId).toList();
    }

    public @Nullable Node getRedirectTarget(Node node) {
        final Integer target = node.redirectTarget();
        return target == null ? null : resolveId(target);
    }

    @Contract("-> new")
    public DeclareCommandsPacket createPacket() {
        return new DeclareCommandsPacket(nodes.stream().map(Node::getPacketNode).toList(), root.id());
    }
}
