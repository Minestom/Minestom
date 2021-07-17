package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

public class RandomLookAroundGoal extends GoalSelector {
    private static final Random RANDOM = new Random();
    private final int chancePerTick;
    private final Supplier<Integer> minimalLookTimeSupplier;
    private final Function<EntityCreature, Vec> randomDirectionFunction;
    private Vec lookDirection;
    private int lookTime = 0;

    public RandomLookAroundGoal(EntityCreature entityCreature, int chancePerTick) {
        this(entityCreature, chancePerTick,
                // These two functions act similarly enough to how MC randomly looks around.

                // Look in one direction for at most 40 ticks and at minimum 20 ticks.
                () -> 20 + RANDOM.nextInt(20),
                // Look at a random block
                (creature) -> {
                    final double n = Math.PI * 2 * RANDOM.nextDouble();
                    return new Vec(
                            (float) Math.cos(n),
                            0,
                            (float) Math.sin(n)
                    );
                });
    }

    /**
     * @param entityCreature          Creature that should randomly look around.
     * @param chancePerTick           The chance (per tick) that the entity looks around. Setting this to N would mean there is a 1 in N chance.
     * @param minimalLookTimeSupplier A supplier that returns the minimal amount of time an entity looks in a direction.
     * @param randomDirectionFunction A function that returns a random vector that the entity will look in/at.
     */
    public RandomLookAroundGoal(
            EntityCreature entityCreature,
            int chancePerTick,
            @NotNull Supplier<Integer> minimalLookTimeSupplier,
            @NotNull Function<EntityCreature, Vec> randomDirectionFunction) {
        super(entityCreature);
        this.chancePerTick = chancePerTick;
        this.minimalLookTimeSupplier = minimalLookTimeSupplier;
        this.randomDirectionFunction = randomDirectionFunction;
    }

    @Override
    public boolean shouldStart() {
        if (RANDOM.nextInt(chancePerTick) != 0) {
            return false;
        }
        return entityCreature.getNavigator().getPathPosition() == null;
    }

    @Override
    public void start() {
        lookTime = minimalLookTimeSupplier.get();
        lookDirection = randomDirectionFunction.apply(entityCreature);
    }

    @Override
    public void tick(long time) {
        --lookTime;
        entityCreature.refreshPosition(entityCreature.getPosition().withDirection(lookDirection));
    }

    @Override
    public boolean shouldEnd() {
        return this.lookTime < 0;
    }

    @Override
    public void end() {
    }
}
