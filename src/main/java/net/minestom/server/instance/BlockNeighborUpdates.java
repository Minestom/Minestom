package net.minestom.server.instance;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.utils.chunk.ChunkCache;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/** Vanilla's {@code CollectingNeighborUpdater}: nested changes run before the next neighbor, and a chain grows a queue, not the stack. */
final class BlockNeighborUpdates {
    // vanilla NeighborUpdater.UPDATE_ORDER
    private static final BlockFace[] ORDER = {
            BlockFace.WEST, BlockFace.EAST, BlockFace.BOTTOM, BlockFace.TOP, BlockFace.NORTH, BlockFace.SOUTH
    };

    private final InstanceContainer instance;
    private final ArrayDeque<Update> stack = new ArrayDeque<>();
    private final List<Update> addedThisLayer = new ArrayList<>();
    private boolean running;

    BlockNeighborUpdates(InstanceContainer instance) {
        this.instance = instance;
    }

    void run(BlockVec position, Block previousBlock, Block block) {
        if (running) {
            // a handler's change: one step further, run after the handler returns
            addedThisLayer.add(new Update(position, previousBlock, block, stack.peek().distance + 1));
            return;
        }
        stack.push(new Update(position, previousBlock, block, 0));
        runUpdates();
    }

    private void runUpdates() {
        running = true;
        try {
            while (!stack.isEmpty() || !addedThisLayer.isEmpty()) {
                for (int i = addedThisLayer.size() - 1; i >= 0; i--) stack.push(addedThisLayer.get(i));
                addedThisLayer.clear();
                final Update next = stack.peek();
                while (addedThisLayer.isEmpty()) {
                    if (!next.runNext()) {
                        stack.pop();
                        break;
                    }
                }
            }
        } finally {
            stack.clear();
            addedThisLayer.clear();
            running = false;
        }
    }

    private final class Update {
        private final BlockVec position;
        private final Block previousBlock;
        private final Block block;
        private final int distance;
        private final ChunkCache cache = new ChunkCache(instance, null, null);
        private int index;

        Update(BlockVec position, Block previousBlock, Block block, int distance) {
            this.position = position;
            this.previousBlock = previousBlock;
            this.block = block;
            this.distance = distance;
        }

        boolean runNext() {
            final BlockFace face = ORDER[index++];
            final var direction = face.toDirection();
            final int x = position.blockX() + direction.normalX();
            final int y = position.blockY() + direction.normalY();
            final int z = position.blockZ() + direction.normalZ();
            final var dimension = instance.getCachedDimensionType();
            if (y >= dimension.minY() && y <= dimension.height()) {
                final Block neighbor = cache.getBlock(x, y, z, Block.Getter.Condition.NONE);
                final BlockHandler handler = neighbor != null ? neighbor.handler() : null;
                if (handler != null && distance < handler.maxNeighborUpdateDistance()) {
                    handler.onNeighborChange(new BlockHandler.NeighborChange(neighbor, instance, new BlockVec(x, y, z),
                            face.getOppositeFace(), position, previousBlock, block));
                }
            }
            return index < ORDER.length;
        }
    }
}
