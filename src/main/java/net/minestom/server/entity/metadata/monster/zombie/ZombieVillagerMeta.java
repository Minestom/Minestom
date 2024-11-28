package net.minestom.server.entity.metadata.monster.zombie;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.*;
import net.minestom.server.entity.metadata.villager.VillagerMeta;
import org.jetbrains.annotations.NotNull;

public class ZombieVillagerMeta extends ZombieMeta {
    public ZombieVillagerMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isConverting() {
        return metadata.get(MetadataDef.ZombieVillager.IS_CONVERTING);
    }

    public void setConverting(boolean value) {
        metadata.set(MetadataDef.ZombieVillager.IS_CONVERTING, value);
    }

    public VillagerMeta.VillagerData getVillagerData() {
        int[] data = metadata.get(MetadataDef.ZombieVillager.VILLAGER_DATA);
        if (data == null) {
            return new VillagerMeta.VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, VillagerMeta.Level.NOVICE);
        }
        return new VillagerMeta.VillagerData(VillagerType.values()[data[0]], VillagerProfession.fromId(data[1]), VillagerMeta.Level.VALUES[data[2] - 1]);
    }

    public void setVillagerData(@NotNull VillagerMeta.VillagerData data) {
        int[] value = new int[]{data.villagerType().ordinal(), data.villagerProfession().id(), data.level().ordinal() + 1};
        metadata.set(MetadataDef.Villager.VARIANT, value);
    }

}
