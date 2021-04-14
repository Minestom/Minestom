package net.minestom.server.command.builder;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NodeMaker {

    private final List<ConfiguredNodes> configuredNodes = new ArrayList<>(2);
    private final List<DeclareCommandsPacket.Node[]> nodes = new ArrayList<>(2);
    private final Object2IntMap<DeclareCommandsPacket.Node> nodeIdsMap = new Object2IntOpenHashMap<>();

    private final List<Pair<String, Request>> nodeRequests = new ArrayList<>();

    public NodeMaker(@NotNull DeclareCommandsPacket.Node[] commandNodes, int id) {
        addNodes(commandNodes);
        for (DeclareCommandsPacket.Node node : commandNodes) {
            this.nodeIdsMap.put(node, id);
        }
    }

    public ConfiguredNodes getLatestConfiguredNodes() {
        if (configuredNodes.isEmpty())
            return null;
        return configuredNodes.get(configuredNodes.size() - 1);
    }

    public DeclareCommandsPacket.Node[] getLatestNodes() {
        ConfiguredNodes configuredNodes = getLatestConfiguredNodes();
        return configuredNodes != null ? configuredNodes.nodes : null;
    }

    public int getNodesCount() {
        return nodes.size();
    }

    public void addNodes(@NotNull DeclareCommandsPacket.Node[] nodes) {
        Options options = new Options();
        this.configuredNodes.add(ConfiguredNodes.of(nodes, options));
        this.nodes.add(nodes);
    }

    @NotNull
    public List<ConfiguredNodes> getConfiguredNodes() {
        return configuredNodes;
    }

    public List<DeclareCommandsPacket.Node[]> getNodes() {
        return nodes;
    }

    @NotNull
    public Object2IntMap<DeclareCommandsPacket.Node> getNodeIdsMap() {
        return nodeIdsMap;
    }

    public void request(String input, Request request) {
        this.nodeRequests.add(Pair.of(input, request));
    }

    public List<Pair<String, Request>> getNodeRequests() {
        return nodeRequests;
    }

    public static class ConfiguredNodes {
        private DeclareCommandsPacket.Node[] nodes;
        private Options options;

        private static ConfiguredNodes of(DeclareCommandsPacket.Node[] nodes, Options options) {
            ConfiguredNodes configuredNodes = new ConfiguredNodes();
            configuredNodes.nodes = nodes;
            configuredNodes.options = options;
            return configuredNodes;
        }

        public DeclareCommandsPacket.Node[] getNodes() {
            return nodes;
        }

        public Options getOptions() {
            return options;
        }
    }

    public static class Options {

        private boolean updateLastNode = true;
        private DeclareCommandsPacket.Node[] previousNodes;

        public static Options init() {
            return new Options();
        }

        public boolean shouldUpdateLastNode() {
            return updateLastNode;
        }

        public Options updateLastNode(boolean updateLastNode) {
            this.updateLastNode = updateLastNode;
            return this;
        }

        public DeclareCommandsPacket.Node[] getPreviousNodes() {
            return previousNodes;
        }

        public Options setPreviousNodes(DeclareCommandsPacket.Node[] previousNodes) {
            this.previousNodes = previousNodes;
            return this;
        }
    }

    @FunctionalInterface
    public interface Request {
        void retrieve(int id);
    }

}
