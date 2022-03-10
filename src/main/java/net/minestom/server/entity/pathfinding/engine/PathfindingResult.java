package net.minestom.server.entity.pathfinding.engine;

import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface PathfindingResult {
    /**
     * Returns a future that will be completed when the path is fully computed.
     * <br><br>
     * This future may return a null value if the path was not able to be completed.
     * @return a future
     */
    @NotNull CompletableFuture<Queue<Point>> fullPath();

    /**
     * Creates an iterator that iterates over all future path sections.
     * <br><br>
     * This iterator will throw a (@link IllegalOperationException} if you call {@link Iterator#next()} while the
     * current future is not completed.
     * <br>
     * This iterator's {@link Iterator#hasNext()} will return {@code true} if there is a possibility of a next future.
     * This means that if you call {@link Iterator#hasNext()} after the current future is completed, it will return
     * true if the path is not completed, and false if the path is completed.
     * <br><br>
     * An example usecase for this method would be for dynamic pathfinding, where the path is changed in real time.
     * @return an iterator
     */
    @NotNull Iterator<CompletableFuture<Queue<Point>>> createPathIterator();

    /**
     * Creates a completed pathfinding result.
     * @param path the path
     * @return the completed pathfinding result
     */
    static PathfindingResult completed(@Nullable Queue<Point> path) {
        return futureCompleted(CompletableFuture.completedFuture(path));
    }

    /**
     * Creates a pathfinding result that will be completed when the given completable future is completed.
     * @param path the path
     * @return the completing pathfinding result
     */
    static PathfindingResult futureCompleted(@NotNull CompletableFuture<Queue<Point>> path) {
        return new PathfindingResult() {

            @Override
            public @NotNull CompletableFuture<@Nullable Queue<Point>> fullPath() {
                return path;
            }

            @Override
            public @NotNull Iterator<CompletableFuture<Queue<Point>>> createPathIterator() {
                return new Iterator<>() {
                    private boolean hasNext = true;
                    @Override
                    public boolean hasNext() {
                        return hasNext;
                    }

                    @Override
                    public CompletableFuture<Queue<Point>> next() {
                        hasNext = false;
                        return fullPath();
                    }
                };
            }
        };
    }
}
