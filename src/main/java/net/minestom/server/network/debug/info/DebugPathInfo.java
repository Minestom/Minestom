package net.minestom.server.network.debug.info;

import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;

import java.util.List;
import java.util.Set;

public record DebugPathInfo(Path path, float maxNodeDistance) {
    public static final NetworkBuffer.Type<DebugPathInfo> SERIALIZER = NetworkBufferTemplate.template(
            Path.SERIALIZER, DebugPathInfo::path,
            NetworkBuffer.FLOAT, DebugPathInfo::maxNodeDistance,
            DebugPathInfo::new);

    public record Path(boolean reached, int nextNodeIndex, Point target, List<Node> nodes, Data data) {
        public static final NetworkBuffer.Type<Path> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.BOOLEAN, Path::reached,
                NetworkBuffer.INT, Path::nextNodeIndex,
                NetworkBuffer.BLOCK_POSITION, Path::target,
                Node.SERIALIZER.list(), Path::nodes,
                Data.SERIALIZER, Path::data,
                Path::new);
    }

    public enum NodeType {
        BLOCKED,
        OPEN,
        WALKABLE,
        WALKABLE_DOOR,
        TRAPDOOR,
        POWDER_SNOW,
        DANGER_POWDER_SNOW,
        FENCE,
        LAVA,
        WATER,
        WATER_BORDER,
        RAIL,
        UNPASSABLE_RAIL,
        DANGER_FIRE,
        DAMAGE_FIRE,
        DANGER_OTHER,
        DAMAGE_OTHER,
        DOOR_OPEN,
        DOOR_WOOD_CLOSED,
        DOOR_IRON_CLOSED,
        BREACH,
        LEAVES,
        STICKY_HONEY,
        COCOA,
        DAMAGE_CAUTIOUS,
        DANGER_TRAPDOOR;

        public static NetworkBuffer.Type<NodeType> SERIALIZER = NetworkBuffer.Enum(NodeType.class);
    }

    public static final class Node {
        public static final NetworkBuffer.Type<Node> SERIALIZER = NetworkBufferTemplate.template(
                NetworkBuffer.INT, Node::x,
                NetworkBuffer.INT, Node::y,
                NetworkBuffer.INT, Node::z,
                NetworkBuffer.FLOAT, Node::walkedDistance,
                NetworkBuffer.FLOAT, Node::costMalus,
                NetworkBuffer.BOOLEAN, Node::closed,
                NodeType.SERIALIZER, Node::type,
                NetworkBuffer.FLOAT, Node::f,
                Node::new);

        private final int x;
        private final int y;
        private final int z;
        private final float walkedDistance;
        private final float costMalus;
        private final boolean closed;
        private final NodeType type;
        private final float f;

        public Node(int x, int y, int z, float walkedDistance, float costMalus, boolean closed, NodeType type, float f) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.walkedDistance = walkedDistance;
            this.costMalus = costMalus;
            this.closed = closed;
            this.type = type;
            this.f = f;
        }

        public int x() {
            return x;
        }

        public int y() {
            return y;
        }

        public int z() {
            return z;
        }

        public float walkedDistance() {
            return walkedDistance;
        }

        public float costMalus() {
            return costMalus;
        }

        public boolean closed() {
            return closed;
        }

        public NodeType type() {
            return type;
        }

        public float f() {
            return f;
        }

    }

    public record Data(Set<Node> targetNodes, List<Node> openSet, List<Node> closedSet) {
        public static final NetworkBuffer.Type<Data> SERIALIZER = NetworkBufferTemplate.template(
                Node.SERIALIZER.set(), Data::targetNodes,
                Node.SERIALIZER.list(), Data::openSet,
                Node.SERIALIZER.list(), Data::closedSet,
                Data::new);
    }
}
