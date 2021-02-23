package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;

/**
 * Created by k.shandurenko on 23.02.2021
 */
public class EntityZombieVillager extends EntityZombie {

    public EntityZombieVillager(@NotNull Position spawnPosition) {
        super(EntityType.ZOMBIE_VILLAGER, spawnPosition);
    }

    public boolean isConverting() {
        return this.metadata.getIndex((byte) 18, false);
    }

    public void setConverting(boolean value) {
        this.metadata.setIndex((byte) 18, Metadata.Boolean(value));
    }

    public int[] getVillagerData() {
        return this.metadata.getIndex((byte) 19, new int[]{});
    }

    public void setVillagerData(int[] value) {
        this.metadata.setIndex((byte) 19, Metadata.VillagerData(value[0], value[1], value[2]));
    }

}
