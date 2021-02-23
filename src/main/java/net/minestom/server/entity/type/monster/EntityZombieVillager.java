package net.minestom.server.entity.type.monster;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.type.ambient.EntityAbstractVillager;
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

    public EntityAbstractVillager.VillagerData getVillagerData() {
        int[] data = this.metadata.getIndex((byte) 17, null);
        if (data == null) {
            return new EntityAbstractVillager.VillagerData(EntityAbstractVillager.Type.PLAINS, EntityAbstractVillager.Profession.NONE, EntityAbstractVillager.Level.NOVICE);
        }
        return new EntityAbstractVillager.VillagerData(EntityAbstractVillager.Type.VALUES[data[0]], EntityAbstractVillager.Profession.VALUES[data[1]], EntityAbstractVillager.Level.VALUES[data[2] - 1]);
    }

    public void setVillagerData(EntityAbstractVillager.VillagerData data) {
        this.metadata.setIndex((byte) 17, Metadata.VillagerData(
                data.getType().ordinal(),
                data.getProfession().ordinal(),
                data.getLevel().ordinal() + 1
        ));
    }

}
