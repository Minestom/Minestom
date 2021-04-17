package net.minestom.server.entity.metadata.villager;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class VillagerMeta extends AbstractVillagerMeta {

    public VillagerMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    @NotNull
    public VillagerData getVillagerData() {
        int[] data = super.metadata.getIndex((byte) 17, null);
        if (data == null) {
            return new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, Level.NOVICE);
        }
        return new VillagerData(VillagerType.values().get(data[0]), VillagerProfession.values().get(data[1]), Level.VALUES[data[2] - 1]);
    }

    public void setVillagerData(@NotNull VillagerData data) {
        super.metadata.setIndex((byte) 17, Metadata.VillagerData(
                data.type.getNumericalId(),
                data.profession.getNumericalId(),
                data.level.ordinal() + 1
        ));
    }

    public static class VillagerData {

        private VillagerType type;
        private VillagerProfession profession;
        private Level level;

        public VillagerData(@NotNull VillagerType type, @NotNull VillagerProfession profession, @NotNull Level level) {
            this.type = type;
            this.profession = profession;
            this.level = level;
        }

        @NotNull
        public VillagerType getType() {
            return this.type;
        }

        public void setType(@NotNull VillagerType type) {
            this.type = type;
        }

        @NotNull
        public VillagerProfession getProfession() {
            return this.profession;
        }

        public void setProfession(@NotNull VillagerProfession profession) {
            this.profession = profession;
        }

        @NotNull
        public Level getLevel() {
            return level;
        }

        public void setLevel(@NotNull Level level) {
            this.level = level;
        }
    }


    public enum Level {
        NOVICE,
        APPRENTICE,
        JOURNEYMAN,
        EXPERT,
        MASTER;

        public final static Level[] VALUES = values();
    }

}
