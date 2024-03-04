package net.minestom.server.entity.pathfinding;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class PPath {
    private final Runnable onComplete;
    private final List<PNode> nodes = new ArrayList<>();

    private final double pathVariance;
    private final double maxDistance;
    private int index = 0;
    private final AtomicReference<PathState> state = new AtomicReference<>(PathState.CALCULATING);

    public Point getNext() {
        if (index + 1 >= nodes.size()) return null;
        var current = nodes.get(index + 1);
        return new Vec(current.x(), current.y(), current.z());
    }

    public void setState(@NotNull PathState newState) {
        state.set(newState);
    }

    public enum PathState {
        CALCULATING,
        FOLLOWING,
        TERMINATING, TERMINATED, COMPUTED, BEST_EFFORT, INVALID
    }

    @NotNull PathState getState() {
        return state.get();
    }

    public @NotNull List<PNode> getNodes() {
        return nodes;
    }

    public PPath(double maxDistance, double pathVariance, Runnable onComplete) {
        this.onComplete = onComplete;
        this.maxDistance = maxDistance;
        this.pathVariance = pathVariance;
    }

    void runComplete() {
        if (onComplete != null) onComplete.run();
    }

    @Override
    public @NotNull String toString() {
        return nodes.toString();
    }

    @Nullable
    PNode.NodeType getCurrentType() {
        if (index >= nodes.size()) return null;
        var current = nodes.get(index);
        return current.getType();
    }

    @Nullable
    Point getCurrent() {
        if (index >= nodes.size()) return null;
        var current = nodes.get(index);
        return new Vec(current.x(), current.y(), current.z());
    }

    void next() {
        if (index >= nodes.size()) return;
        index++;
    }

    double maxDistance() {
        return maxDistance;
    }

    double pathVariance() {
        return pathVariance;
    }
}
