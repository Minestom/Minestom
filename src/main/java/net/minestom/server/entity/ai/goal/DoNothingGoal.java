package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.Goal;
import net.minestom.server.utils.MathUtils;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class DoNothingGoal extends Goal {

    private static final Random RANDOM = new Random();

    private final long time;
    private final float chance;
    private long startTime;

    /**
     * Create a DoNothing goal
     *
     * @param entityCreature the entity
     * @param time           the time in milliseconds where nothing happen
     * @param chance         the chance to do nothing (0-1)
     */
    public DoNothingGoal(EntityCreature entityCreature, long time, float chance) {
        super(entityCreature);
        this.time = TimeUnit.MILLISECONDS.toNanos(time);
        this.chance = MathUtils.clamp(chance, 0, 1);
    }

    @Override
    public void end() {
        this.startTime = 0;
    }

    @Override
    public boolean shouldEnd() {
        return System.nanoTime() - startTime >= time;
    }

    @Override
    public boolean canStart() {
        return RANDOM.nextFloat() <= chance;
    }

    @Override
    public void start() {
        this.startTime = System.nanoTime();
    }

    @Override
    public void tick(long time) {

    }
}
