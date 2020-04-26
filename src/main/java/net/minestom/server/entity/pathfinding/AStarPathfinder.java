package net.minestom.server.entity.pathfinding;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.BlockPosition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class AStarPathfinder {

    // TODO ladder, jump, etc...
    // TODO include BoundingBox support

    private boolean canClimbLadder;
    private boolean canSwim;
    private boolean canJump;

    public static LinkedList<BlockPosition> getPath(Instance instance,
                                                    BlockPosition start, BlockPosition end,
                                                    int maxCheck) {
        List<Node> open = new ArrayList<>();
        List<Node> closed = new ArrayList<>();

        Node startNode = Node.fromBlockPosition(start);
        Node endNode = Node.fromBlockPosition(end);

        open.add(startNode);

        int checkCount = 0;

        while (!open.isEmpty()) {
            Node current = getCurrentNode(open);
            open.remove(current);
            closed.add(current);

            if (isTargetNode(end, current)) {
                return buildPath(current);
            }

            for (Node neighbor : getNeighbors(instance, current)) {
                if (isInList(closed, neighbor))
                    continue;

                boolean isInOpen = isInList(open, neighbor);
                if (isShorter(neighbor, current) || !isInOpen) {

                    neighbor.parent = current;
                    neighbor.g = getTentativeGScore(neighbor, current);
                    neighbor.f = neighbor.g + getDistance(neighbor, endNode);
                    if (!isInOpen) {
                        open.add(neighbor);
                    }
                }
            }

            // To do not check the whole world
            checkCount++;
            if (checkCount >= maxCheck)
                break;
        }

        return null;
    }

    private static List<Node> getNeighbors(Instance instance, Node current) {
        List<Node> result = new ArrayList<>();
        BlockPosition currentBlockPosition = current.getBlockPosition();

        for (int x = -1; x < 2; x++) {
            for (int y = -1; y < 2; y++) {
                for (int z = -1; z < 2; z++) {
                    if (x == 0 && y == 0 && z == 0)
                        continue;
                    BlockPosition neighbor = currentBlockPosition.clone().add(x, y, z);
                    if (canAccessBlock(instance, currentBlockPosition, neighbor)) {
                        Node node = Node.fromBlockPosition(neighbor);
                        result.add(node);
                    }
                }
            }
        }

        return result;
    }

    private static Node getCurrentNode(List<Node> open) {
        Node closest = null;

        for (Node node : open) {
            if (closest == null || node.f < closest.f) {
                closest = node;
            }
        }

        return closest;
    }

    private static boolean isTargetNode(BlockPosition target, Node node) {
        return target.getX() == node.getX() &&
                target.getY() == node.getY() &&
                target.getZ() == node.getZ();
    }

    private static boolean canAccessBlock(Instance instance, BlockPosition current, BlockPosition target) {
        if (instance.getChunkAt(target) == null)
            return false;

        Block targetBlock = Block.fromId(instance.getBlockId(target));
        Block belowBlock = Block.fromId(instance.getBlockId(target.clone().add(0, -1, 0)));

        boolean result = targetBlock.isAir() && belowBlock.isSolid();
        return result;
    }

    private static boolean isInList(List<Node> list, Node node) {
        for (Node close : list) {
            if (close.getX() == node.getX() &&
                    close.getY() == node.getY() &&
                    close.getZ() == node.getZ())
                return true;
        }
        return false;
    }

    private static int getDistance(Node node1, Node node2) {
        return node1.blockPosition.getDistance(node2.blockPosition);
    }

    private static int getTentativeGScore(Node neighbor, Node current) {
        return neighbor.g + getDistance(neighbor, current);
    }

    private static boolean isShorter(Node neighbor, Node current) {
        return getTentativeGScore(neighbor, current) < neighbor.g;
    }

    private static LinkedList<BlockPosition> buildPath(Node finalNode) {
        LinkedList<BlockPosition> result = new LinkedList<>();
        Node cache = finalNode;
        while (cache != null) {
            result.add(cache.blockPosition);
            cache = cache.parent;
        }
        Collections.reverse(result);
        return result;
    }

    private static class Node {
        public int g, h, f;
        public Node parent;
        private int x, y, z;
        private BlockPosition blockPosition;

        public Node(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.blockPosition = new BlockPosition(x, y, z);
        }

        public static Node fromBlockPosition(BlockPosition blockPosition) {
            Node node = new Node(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
            return node;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getZ() {
            return z;
        }

        public BlockPosition getBlockPosition() {
            return blockPosition;
        }
    }
}
