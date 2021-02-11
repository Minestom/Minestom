package net.minestom.server.command.builder;

import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NodeMaker {

    private DeclareCommandsPacket.Node[] lastNodes;
    private DeclareCommandsPacket.Node[] currentNodes;

    public DeclareCommandsPacket.Node[] getCurrentNodes() {
        return currentNodes;
    }

    public void addNodes(@NotNull DeclareCommandsPacket.Node[] nodes) {
        this.currentNodes = nodes;
    }

    /**
     * Represents the nodes computed in the last iteration.
     *
     * @return the previous nodes, null if none
     */
    @Nullable
    public DeclareCommandsPacket.Node[] getLastNodes() {
        return lastNodes;
    }

    public void setLastNodes(DeclareCommandsPacket.Node[] lastNodes) {
        this.lastNodes = lastNodes;
    }
}
