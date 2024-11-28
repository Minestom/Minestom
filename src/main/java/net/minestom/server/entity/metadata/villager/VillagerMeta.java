package net.minestom.server.entity.metadata.villager;

import net.minestom.server.entity.*;
import org.jetbrains.annotations.NotNull;

public class VillagerMeta extends AbstractVillagerMeta {
    public VillagerMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    @NotNull
    public VillagerData getVillagerData() {
        int[] data = metadata.get(MetadataDef.Villager.VARIANT);
        if (data == null) {
            return new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, Level.NOVICE);
        }
        return new VillagerData(VillagerType.values()[data[0]], VillagerProfession.fromId(data[1]), Level.VALUES[data[2] - 1]);
    }

    public void setVillagerData(@NotNull VillagerData data) {
        int[] value = new int[]{data.villagerType.ordinal(), data.villagerProfession.id(), data.level.ordinal() + 1};
        metadata.set(MetadataDef.Villager.VARIANT, value);
    }

    public record VillagerData(@NotNull VillagerType villagerType, @NotNull VillagerProfession villagerProfession, @NotNull Level level) { }

    public enum Level {
        NOVICE,
        APPRENTICE,
        JOURNEYMAN,
        EXPERT,
        MASTER;

        public final static Level[] VALUES = values();
    }

}
