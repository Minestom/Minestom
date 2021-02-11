package net.minestom.server.command.builder;

import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NodeMaker {

    private final List<DeclareCommandsPacket.Node[]> nodes = new ArrayList<>(2);

    public DeclareCommandsPacket.Node[] getLatestNodes() {
        if (nodes.isEmpty())
            return null;
        return nodes.get(nodes.size() - 1);
    }

    public void addNodes(@NotNull DeclareCommandsPacket.Node[] nodes) {
        this.nodes.add(nodes);
    }

    @NotNull
    public List<DeclareCommandsPacket.Node[]> getNodes() {
        return nodes;
    }
}
