package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.pathfinding.task.PathfindTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RandomStrollGoal extends GoalSelector {

    private static final long DELAY = 2500;

    private final int radius;
    private final List<Vec> closePositions;
    private final Random random = new Random();

    // Pathfinding path
    private @Nullable PathfindTask.Path path;

    private long lastStroll;

    public RandomStrollGoal(@NotNull EntityCreature entityCreature, int radius) {
        super(entityCreature);
        this.radius = radius;
        this.closePositions = getNearbyBlocks(radius);
    }

    @Override
    public boolean shouldStart() {
        return System.currentTimeMillis() - lastStroll >= DELAY;
    }

    @Override
    public void start() {
        final int remainingAttempt = closePositions.size();
        CompletableFuture.runAsync(() -> {
            for (int i = 0; i < remainingAttempt; i++) {
                final int index = random.nextInt(closePositions.size());
                final Vec position = closePositions.get(index);

                final var target = entityCreature.getPosition().add(position);
                if (path == null) {
                    path = PathfindTask.moveTo(target).start(entityCreature);
                    Queue<Point> fullPath = path.fullPath();
                    if (fullPath == null) {
                        // Pathfind failed, go next
                        path.updateTarget(null);
                    } else {
                        // Pathfind success, return
                        return;
                    }
                }
            }
        });
    }

    @Override
    public void tick(long time) {
    }

    @Override
    public boolean shouldEnd() {
        return true;
    }

    @Override
    public void end() {
        this.lastStroll = System.currentTimeMillis();
    }

    public int getRadius() {
        return radius;
    }

    private static @NotNull List<Vec> getNearbyBlocks(int radius) {
        List<Vec> blocks = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    blocks.add(new Vec(x, y, z));
                }
            }
        }
        return blocks;
    }
}
