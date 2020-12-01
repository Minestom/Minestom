package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.BlockPosition;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Random;

public class EatBlockGoal extends GoalSelector {
    private static final Random RANDOM = new Random();
    private final Map<Short, Short> eatBelowMap;
    private final Map<Short, Short> eatInMap;
    private final int chancePerTick;
    private int eatAnimationTick;

    /**
     * @param eatInMap       Map containing the block IDs that the entity can eat (when inside the block) and the block ID of the replacement block.
     * @param eatBelowMap    Map containing block IDs that the entity can eat (when above the block) and the block ID of the replacement block.
     * @param chancePerTick  The chance (per tick) that the entity eats. Settings this to N would mean there is a 1 in N chance.
     */
    public EatBlockGoal(
            @NotNull Map<Short, Short> eatInMap,
            @NotNull Map<Short, Short> eatBelowMap,
            int chancePerTick) {
        this.eatInMap = eatInMap;
        this.eatBelowMap = eatBelowMap;
        this.chancePerTick = chancePerTick;
    }

    @Override
    public boolean shouldStart(@NotNull EntityCreature entityCreature) {
        // TODO: is Baby
        if (RANDOM.nextInt(chancePerTick) != 0) {
            return false;
        }
        final Instance instance = entityCreature.getInstance();
        final BlockPosition blockPosition = entityCreature.getPosition().toBlockPosition();
        final short blockStateIdIn = instance.getBlockStateId(blockPosition.copy().subtract(0, 1, 0));
        final short blockStateIdBelow = instance.getBlockStateId(blockPosition.copy().subtract(0, 2, 0));

        return eatInMap.containsKey(blockStateIdIn) || eatBelowMap.containsKey(blockStateIdBelow);
    }

    @Override
    public void start(@NotNull EntityCreature entityCreature) {
        this.eatAnimationTick = 40;
        // TODO: EatBlockEvent call here.
        // Stop moving
        entityCreature.setPathTo(null);
    }

    @Override
    public void tick(@NotNull EntityCreature entityCreature, long time) {
        this.eatAnimationTick = Math.max(0, this.eatAnimationTick - 1);
        if (this.eatAnimationTick != 4) {
            return;
        }
        Instance instance = entityCreature.getInstance();
        final BlockPosition currentPosition = entityCreature.getPosition().toBlockPosition().copy().subtract(0, 1, 0);
        final BlockPosition belowPosition = currentPosition.copy().subtract(0, 1, 0);

        final short blockStateIdIn = instance.getBlockStateId(currentPosition);
        final short blockStateIdBelow = instance.getBlockStateId(belowPosition);
        if (eatInMap.containsKey(blockStateIdIn)) {
            instance.setBlockStateId(currentPosition, eatInMap.get(blockStateIdIn));
        } else if (eatBelowMap.containsKey(blockStateIdBelow)) {
            instance.setBlockStateId(belowPosition, eatBelowMap.get(blockStateIdBelow));
        }
        // TODO: Call Entity Eat Animation
    }

    @Override
    public boolean shouldEnd(@NotNull EntityCreature entityCreature) {
        return eatAnimationTick <= 0;
    }

    @Override
    public void end(@NotNull EntityCreature entityCreature) {
        this.eatAnimationTick = 0;
    }
}
