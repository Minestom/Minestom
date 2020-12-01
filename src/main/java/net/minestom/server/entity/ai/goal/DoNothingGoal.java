package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class DoNothingGoal extends GoalSelector {

    private static final Random RANDOM = new Random();

    private final long time;
    private final float chance;
    private long startTime;

    /**
     * Create a DoNothing goal
     *
     * @param time           the time in milliseconds where nothing happen
     * @param chance         the chance to do nothing (0-1)
     */
    public DoNothingGoal(long time, float chance) {
        this.time = time;
        this.chance = MathUtils.clampFloat(chance, 0, 1);
    }

    @Override
    public void end(@NotNull EntityCreature entityCreature) {
        this.startTime = 0;
    }

    @Override
    public boolean shouldEnd(@NotNull EntityCreature entityCreature) {
        return System.currentTimeMillis() - startTime >= time;
    }

    @Override
    public boolean shouldStart(@NotNull EntityCreature entityCreature) {
        return RANDOM.nextFloat() <= chance;
    }

    @Override
    public void start(@NotNull EntityCreature entityCreature) {
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public void tick(@NotNull EntityCreature entityCreature, long time) {

    }
}
