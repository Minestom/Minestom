package net.minestom.server.collision;

import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.block.BlockIterator;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * A modifiable queue for more advanced block raycasts.
 * <p>
 * Use {@link Ray#blockQueue} to create.
 */
public class BlockQueue extends ArrayDeque<Ray.Intersection<Block>> {
    private final Iterator<Collection<Ray.Intersection<Block>>> refiller;

    /**
     * Create a {@link BlockQueue} with the specified refiller.
     * @param refiller something that can provide collisions in steps
     *                 <p>
     *                 {@link Ray#blockQueue(Block.Getter)} can handle this automatically
     */
    public BlockQueue(Iterator<Collection<Ray.Intersection<Block>>> refiller) {
        super();
        this.refiller = refiller;
    }

    /**
     * Refill this queue with zero or more results.
     * @return number of entries added
     */
    public int refill() {
        if (!refiller.hasNext()) return 0;
        Collection<Ray.Intersection<Block>> next = refiller.next();
        addAll(next);
        return next.size();
    }

    /**
     * Keep refilling until something is added or the refiller cannot add anything more.
     * @return number of entries added, zero if refiller does not have a next element
     */
    public int refillSome() {
        while (refiller.hasNext()) {
            int result = refill();
            if (result > 0) return result;
        }
        return 0;
    }

    /**
     * Keep refilling until the refiller does not have a next element.
     * @return number of entries added
     */
    public int refillAll() {
        int added = 0;
        while (refiller.hasNext()) {
            added += refill();
        }
        return added;
    }

    /**
     * If the first and second elements exist and {@link net.minestom.server.collision.Ray.Intersection#canMerge(Ray.Intersection) can merge},
     * merge them, otherwise do nothing
     * @param predicate a predicate for merging
     * @return whether elements were merged
     */
    public boolean merge(BiPredicate<Ray.Intersection<Block>, Ray.Intersection<Block>> predicate) {
        if (isEmpty()) return false;
        Ray.Intersection<Block> first = poll();
        Ray.Intersection<Block> next = peek();
        if (next == null || !first.canMerge(next) || !predicate.test(first, next)) {
            addFirst(first);
            return false;
        }
        remove();
        addFirst(first.merge(next));
        return true;
    }

    /**
     * If the first and second elements exist and {@link net.minestom.server.collision.Ray.Intersection#canMerge(Ray.Intersection) can merge},
     * merge them, otherwise do nothing
     * @return whether elements were merged
     */
    public boolean merge() {
        return merge((_, _) -> true);
    }

    /**
     * {@link #merge(BiPredicate) Merge} for as long as possible.
     * @param predicate a predicate for merging
     * @return number of times merged
     */
    public int mergeAll(BiPredicate<Ray.Intersection<Block>, Ray.Intersection<Block>> predicate) {
        int merged = 0;
        while (merge(predicate)) merged++;
        return merged;
    }

    /**
     * {@link #merge() Merge} for as long as possible.
     * @return number of times merged
     */
    public int mergeAll() {
        int merged = 0;
        while (merge()) merged++;
        return merged;
    }
}
