package net.minestom.server.entity.ai.goal;

import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.utils.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RandomStrollGoal extends GoalSelector {

    private static final long DELAY = 2500;

    private int radius;
    private List<Position> closePositions;

    private long lastStroll;

    public RandomStrollGoal(EntityCreature entityCreature, int radius) {
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
        Collections.shuffle(closePositions);

        for (Position position : closePositions) {
            final Position target = position.clone().add(entityCreature.getPosition());
            final boolean result = entityCreature.setPathTo(target);
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

    private List<Position> getNearbyBlocks(int radius) {
        List<Position> blocks = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    final Position position = new Position(x, y, z);
                    blocks.add(position);
                }
            }
        }
        return blocks;
    }

}
