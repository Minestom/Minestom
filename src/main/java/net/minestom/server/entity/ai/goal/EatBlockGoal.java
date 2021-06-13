package net.minestom.server.entity.ai.goal;

import it.unimi.dsi.fastutil.shorts.Short2ShortArrayMap;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.world.World;
import net.minestom.server.block.Block;
import net.minestom.server.utils.BlockPosition;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class EatBlockGoal extends GoalSelector {
    private static final Random RANDOM = new Random();
    private final Short2ShortArrayMap eatBelowMap;
    private final Short2ShortArrayMap eatInMap;
    private final int chancePerTick;
    private int eatAnimationTick;

    /**
     * @param entityCreature Creature that should eat a block.
     * @param eatInMap       Map containing the block IDs that the entity can eat (when inside the block) and the block ID of the replacement block.
     * @param eatBelowMap    Map containing block IDs that the entity can eat (when above the block) and the block ID of the replacement block.
     * @param chancePerTick  The chance (per tick) that the entity eats. Settings this to N would mean there is a 1 in N chance.
     */
    public EatBlockGoal(
            @NotNull EntityCreature entityCreature,
            @NotNull Short2ShortArrayMap eatInMap,
            @NotNull Short2ShortArrayMap eatBelowMap,
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

        final World world = entityCreature.getWorld();

        // An entity shouldn't be eating blocks of a null World.
        if (world == null) {
            return false;
        }

        final BlockPosition blockPosition = entityCreature.getPosition().toBlockPosition();
        final short blockStateIdIn = world.getBlock(blockPosition.clone().subtract(0, 1, 0)).getStateId();
        final short blockStateIdBelow = world.getBlock(blockPosition.clone().subtract(0, 2, 0)).getStateId();

        return eatInMap.containsKey(blockStateIdIn) || eatBelowMap.containsKey(blockStateIdBelow);
    }

    @Override
    public void start() {
        this.eatAnimationTick = 40;
        // TODO: EatBlockEvent call here.
        // Stop moving
        entityCreature.getNavigator().setPathTo(null);
    }

    @Override
    public void tick(long time) {
        this.eatAnimationTick = Math.max(0, this.eatAnimationTick - 1);
        if (this.eatAnimationTick != 4) {
            return;
        }
        World world = entityCreature.getWorld();
        final BlockPosition currentPosition = entityCreature.getPosition().toBlockPosition().clone().subtract(0, 1, 0);
        final BlockPosition belowPosition = currentPosition.clone().subtract(0, 1, 0);

        final short blockStateIdIn = world.getBlock(currentPosition).getStateId();
        final short blockStateIdBelow = world.getBlock(belowPosition).getStateId();
        if (eatInMap.containsKey(blockStateIdIn)) {
            world.setBlock(currentPosition, Block.fromStateId(eatInMap.get(blockStateIdIn)));
        } else if (eatBelowMap.containsKey(blockStateIdBelow)) {
            world.setBlock(belowPosition, Block.fromStateId(eatBelowMap.get(blockStateIdBelow)));
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
