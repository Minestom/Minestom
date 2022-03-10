package net.minestom.server.entity.pathfinding.task;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.pathfinding.Navigator;
import net.minestom.server.entity.pathfinding.engine.PathfindingEngine;
import net.minestom.server.entity.pathfinding.engine.PathfindingResult;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiPredicate;

public abstract class StaticMovementTask extends StaticPathfindTask<Navigator, StaticMovementTask.Execution> {

    StaticMovementTask(@NotNull Point target) {
        super(target);
    }

    @Override
    public @NotNull StaticMovementTask.Execution createExecution(@NotNull Navigator navigator) {
        StaticMovementTask.Execution execution = new Execution(
                this,
                navigator,
                this::moveTowards,
                navigator.getNavigator().getPathfindingEngine(this),
                new CompletableFuture<>(),
                new CompletableFuture<>(),
                new CompletableFuture<>()
        );
        execution.setup();
        return execution;
    }

    /**
     * Moves the navigator towards the target position.
     * @param execution the execution
     * @param point the target position
     * @return false if the navigator is not able to move further, true otherwise
     */
    protected abstract boolean moveTowards(Execution execution, Point point);

    /**
     * The execution of the task.
     * @param completion returns true if the full movement was completed, false otherwise
     */
    public record Execution(
            @NotNull StaticMovementTask task,
            @NotNull Navigator navigator,
            @NotNull BiPredicate<Execution, Point> moveTowards,
            @NotNull PathfindingEngine<Navigator> engine,
            @NotNull CompletableFuture<Void> hibernation,
            @NotNull CompletableFuture<PathfindingResult> result,
            @NotNull CompletableFuture<@NotNull Boolean> completion
    ) implements PathfindTask.ExecutionWithResult<Navigator> {

        private void setup() {
            // Once the task starts
            hibernation().thenRun(() -> {
                // Run the pathfind
                PathfindingResult result = task.pathfind(this);

                // Create the iterator to access the future points
                handlePath(result.createPathIterator());
            });
        }

        private void handlePath(Iterator<CompletableFuture<Queue<Point>>> iterator) {
            // Use the iterator to get the future points
            if (iterator.hasNext()) {
                CompletableFuture<Queue<Point>> future = iterator.next();

                // Once the point queue is calculated
                future.thenAccept(points -> {
                    // Move the navigator to each point
                    navigator.nextTick(() -> handleMovementThen(points, () -> {
                        // Once we have moved to each point, we can handle the next point queue
                        handlePath(iterator);
                    }));
                });
            } else {
                // If there is no more future points, there are no more points to walk to
                completion().complete(true);
            }
        }

        private void handleMovementThen(Queue<Point> points, Runnable then) {
            if (points.isEmpty()) {
                then.run();
                return;
            }

            Point target = points.peek();
            assert target != null;
            while (navigator.isTouching(target, 0.2)) {
                // If the target is close enough, remove it from the queue
                target = points.poll();
                if (target == null) {
                    break;
                }
            }
            if (target != null) {
                Point finalTarget = target;
                navigator.nextTick(() -> {
                    boolean isStuck = moveTowards().test(this, finalTarget);
                    if (isStuck) {
                        // If we are stuck, we can't move further
                        completion().complete(false);
                        return;
                    }
                    handleMovementThen(points, then);
                });
            } else {
                then.run();
            }
        }

        @Override
        public @NotNull StaticMovementTask task() {
            return task;
        }
    }
}
