package net.minestom.scratch.block;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public final class BlockEntityHandler<W, A> {
    private final Map<Integer, Entry<W, A>> entries;
    private final Map<Point, Action<A>> blocks = new HashMap<>();

    public BlockEntityHandler(Map<Integer, Entry<W, A>> entries) {
        this.entries = entries;
    }

    public void place(A actor, W world, Point point, Block block) {
        final int id = block.registry().id();
        final Entry entry = entries.get(id);
        if (entry == null) return;
        final Action action = (Action) entry.supplier.apply(world, point);
        blocks.put(point, action);
    }

    public void brk(A actor, Point point) {
        final Action action = blocks.remove(point);
        if (action == null) return;
        action.onBreak(actor);
    }

    public void interact(A actor, Point point) {
        final Action action = blocks.get(point);
        if (action == null) return;
        action.onInteract(actor);
    }

    public record Entry<W, A>(BiFunction<W, Point, ? extends Action<A>> supplier) {
    }

    public interface Action<P> {
        default void onBreak(P player) {
        }

        default void onInteract(P player) {
        }
    }
}
