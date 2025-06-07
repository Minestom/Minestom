package net.minestom.server.entity.ai.goal;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.AIGoal;
import net.minestom.server.entity.ai.target.RandomNearbyPositionTarget;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RandomStrollGoal extends AIGoal {

    private static final long DELAY = 2500;

    private final int radius;
    private final int maxAttempts = 20;

    private long lastStroll;

    public RandomStrollGoal(@NotNull EntityCreature entityCreature, int radius) {
        super(entityCreature, List.of(new RandomNearbyPositionTarget(radius)), 0);
        this.radius = radius;
    }

    @Override
    public boolean shouldStart() {
        return System.currentTimeMillis() - lastStroll >= DELAY;
    }

    @Override
    public void start() {
        int remainingAttempt = maxAttempts;
        while (remainingAttempt-- > 0) {
            final Pos position = findTargetPosition();
            if (position == null)
                continue;

            final var target = entityCreature.getPosition().add(position);
            final boolean result = entityCreature.getNavigator().setPathTo(target);
            if (result) {
                break;
            }
        }
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
}
