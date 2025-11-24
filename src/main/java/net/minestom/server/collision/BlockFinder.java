package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.block.BlockIterator;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * An iterator for collisions along a {@link Ray} using certain providers for blocks and their hitboxes.
 * <p>
 * Use {@link Ray#findBlocks} to create.
 * <p>
 * Keep in mind that while the entry points are always accurate, the exit points may not be in blocks like stairs.
 * <p>
 * For these cases, you can manually {@link net.minestom.server.collision.Ray.Intersection#overlaps(Ray.Intersection) check}
 * and {@link net.minestom.server.collision.Ray.Intersection#merge(Ray.Intersection) merge}, or use a {@link BlockQueue} instead.
 * @param ray the ray to traverse
 * @param blockIterator the block iterator
 * @param blockGetter the block getter, such as an Instance or Chunk
 * @param hitboxGetter a function to get each {@link BoundingBox} from a block
 */
public record BlockFinder(
        Ray ray,
        BlockIterator blockIterator,
        Block.Getter blockGetter,
        Function<Block, Collection<BoundingBox>> hitboxGetter
) implements Iterator<Collection<Ray.Intersection<Block>>> {

    /**
     * A hitbox getter that finds a block's collision hitboxes.
     */
    public static final Function<Block, Collection<BoundingBox>> SOLID_BLOCK_HITBOXES =
            block -> block.registry().collisionShape().boundingBoxes();

    /**
     * A hitbox getter that finds a block's client-side boxes (the outlines you see when looking at a block).
     */
    public static final Function<Block, Collection<BoundingBox>> BLOCK_HITBOXES =
            block -> block.registry().shape().boundingBoxes();

    /**
     * A 1x1x1 block hitbox.
     */
    private static final Collection<BoundingBox> CUBE = List.of(new BoundingBox(Vec.ZERO, Vec.ONE));

    /**
     * A hitbox getter that returns a cube if the block has any solid collision.
     */
    public static final Function<Block, Collection<BoundingBox>> SOLID_CUBE_HITBOXES =
            block -> (block.isSolid() ? CUBE : List.of());

    /**
     * A hitbox getter that returns a cube if the block is not air.
     */
    public static final Function<Block, Collection<BoundingBox>> CUBE_HITBOXES =
            block -> (!block.isAir() ? CUBE : List.of());

    @Override
    public boolean hasNext() {
        return blockIterator.hasNext();
    }

    @Override
    public List<Ray.Intersection<Block>> next() {
        ArrayList<Ray.Intersection<Block>> results = new ArrayList<>();
        if (blockIterator.hasNext()) {
            Point p = blockIterator.next();
            Block b = blockGetter.getBlock(p);
            Collection<BoundingBox> hitboxes = hitboxGetter.apply(b);
            if (!hitboxes.isEmpty()) {
                for (BoundingBox h : hitboxes) {
                    Ray.Intersection<?> r = ray.cast(h, p.asVec());
                    if (r != null) results.add(r.withObject(b));
                }
                if (!results.isEmpty()) {
                    Collections.sort(results);
                    return results;
                }
            }
        }
        return List.of();
    }

    /**
     * Return the next closest intersection.
     * Keep in mind that this discards all other hits within the found block.
     * @return the next closest intersection, or null if there are none
     */
    public @Nullable Ray.Intersection<Block> nextClosest() {
        while (blockIterator.hasNext()) {
            Collection<Ray.Intersection<Block>> results = next();
            if (!results.isEmpty()) return Collections.min(results);
        }
        return null;
    }
}
