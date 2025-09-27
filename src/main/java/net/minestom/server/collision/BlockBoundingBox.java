package net.minestom.server.collision;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;

/**
 * A block-aligned, absolute bounding box.
 *
 * <p>This is in contrast to BoundingBox which is relative to its owner's position, and precise.</p>
 */
public record BlockBoundingBox(Point min, Point max) {
    public static final NetworkBuffer.Type<BlockBoundingBox> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.BLOCK_POSITION, BlockBoundingBox::min,
            NetworkBuffer.BLOCK_POSITION, BlockBoundingBox::max,
            BlockBoundingBox::new);

    public BlockBoundingBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this(new BlockVec(minX, minY, minZ), new BlockVec(maxX, maxY, maxZ));
    }

    public int minX() {
        return min.blockX();
    }

    public int minY() {
        return min.blockY();
    }

    public int minZ() {
        return min.blockZ();
    }

    public int maxX() {
        return max.blockX();
    }

    public int maxY() {
        return max.blockY();
    }

    public int maxZ() {
        return max.blockZ();
    }

}
