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
    private int chancePerTick;
    private int eatAnimationTick;

    /**
     * @param entityCreature Creature that should eat a block.
     * @param eatInMap Map containing the block IDs that the entity can eat (when inside the block) and the block ID of the replacement block.
     * @param eatBelowMap    Map containing block IDs that the entity can eat (when above the block) and the block ID of the replacement block.
     * @param chancePerTick The chance that the entity eats. Settings this to N would mean there is a 1 in N chance.
     */
    public EatBlockGoal(
            @NotNull EntityCreature entityCreature,
            @NotNull Map<Short, Short> eatInMap,
            @NotNull Map<Short, Short> eatBelowMap,
            int chancePerTick) {
        super(entityCreature);
        this.eatInMap = eatInMap;
        this.eatBelowMap = eatBelowMap;
        this.chancePerTick = chancePerTick;
    }

    @Override
    public boolean shouldStart() {
        // TODO: is Baby
        if (RANDOM.nextInt(chancePerTick) != 0) {
            return false;
        }
        Instance instance = entityCreature.getInstance();
        final short blockIdIn = instance.getBlockId(entityCreature.getPosition().toBlockPosition().clone().subtract(0, 1,0));
        final short blockIdBelow = instance.getBlockId(entityCreature.getPosition().toBlockPosition().clone().subtract(0,2,0));

        return eatInMap.containsKey(blockIdIn) || eatBelowMap.containsKey(blockIdBelow);
    }

    @Override
    public void start() {
        this.eatAnimationTick = 40;
        // TODO: EatBlockEvent call here.
        // Stop moving
        entityCreature.setPathTo(null);
    }

    @Override
    public void tick() {
        this.eatAnimationTick = Math.max(0, this.eatAnimationTick - 1);
        if (this.eatAnimationTick != 4) {
            return;
        }
        Instance instance = entityCreature.getInstance();
        final BlockPosition currentPosition = entityCreature.getPosition().toBlockPosition().clone().subtract(0, 1, 0);
        final BlockPosition belowPosition = currentPosition.clone().subtract(0, 1, 0);

        final short blockIdIn = instance.getBlockId(currentPosition);
        final short blockIdBelow = instance.getBlockId(belowPosition);
        if (eatInMap.containsKey(blockIdIn)) {
            instance.setBlock(currentPosition, eatInMap.get(blockIdIn));
        } else if (eatBelowMap.containsKey(blockIdBelow)) {
            instance.setBlock(belowPosition, eatBelowMap.get(blockIdBelow));
        }
        // TODO: Call Entity Eat Animation
    }

    @Override
    public boolean shouldEnd() {
        return eatAnimationTick <= 0;
    }

    @Override
    public void end() {
        this.eatAnimationTick = 0;
    }
}
