package net.minestom.server.entity.ai.target;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ai.TargetSelector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomNearbyPositionTarget implements TargetSelector {

    private final int radius;
    private final Random random = new Random();

    public RandomNearbyPositionTarget(int radius) {
        this.radius = radius;
    }

    @Override
    public @Nullable Entity findTargetEntity(@NotNull EntityCreature entityCreature) {
        return null;
    }

    @Override
    public @Nullable Pos findTargetPosition(@NotNull EntityCreature entityCreature) {
        List<Pos> blocks = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    blocks.add(new Pos(x, y, z));
                }
            }
        }
        int randomIndex = random.nextInt(blocks.size());
        return blocks.get(randomIndex);
    }
}
