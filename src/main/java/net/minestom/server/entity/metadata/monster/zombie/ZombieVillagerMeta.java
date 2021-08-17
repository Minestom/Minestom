package net.minestom.server.entity.metadata.monster.zombie;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.villager.VillagerMeta;
import org.jetbrains.annotations.NotNull;

public class ZombieVillagerMeta extends ZombieMeta {
    public static final byte OFFSET = ZombieMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 2;

    public ZombieVillagerMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isConverting() {
        return super.metadata.getIndex(OFFSET, false);
    }

    public void setConverting(boolean value) {
        super.metadata.setIndex(OFFSET, Metadata.Boolean(value));
    }

    public VillagerMeta.VillagerData getVillagerData() {
        int[] data = super.metadata.getIndex(OFFSET + 1, null);
        if (data == null) {
            return new VillagerMeta.VillagerData(VillagerMeta.Type.PLAINS, VillagerMeta.Profession.NONE, VillagerMeta.Level.NOVICE);
        }
        return new VillagerMeta.VillagerData(VillagerMeta.Type.VALUES[data[0]], VillagerMeta.Profession.VALUES[data[1]], VillagerMeta.Level.VALUES[data[2] - 1]);
    }

    public void setVillagerData(VillagerMeta.VillagerData data) {
        super.metadata.setIndex(OFFSET + 1, Metadata.VillagerData(
                data.getType().ordinal(),
                data.getProfession().ordinal(),
                data.getLevel().ordinal() + 1
        ));
    }

}
