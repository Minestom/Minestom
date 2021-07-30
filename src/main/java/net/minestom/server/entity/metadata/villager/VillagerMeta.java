package net.minestom.server.entity.metadata.villager;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class VillagerMeta extends AbstractVillagerMeta {
    public static final byte OFFSET = AbstractVillagerMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    public VillagerMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    @NotNull
    public VillagerData getVillagerData() {
        int[] data = super.metadata.getIndex(OFFSET, null);
        if (data == null) {
            return new VillagerData(Type.PLAINS, Profession.NONE, Level.NOVICE);
        }
        return new VillagerData(Type.VALUES[data[0]], Profession.VALUES[data[1]], Level.VALUES[data[2] - 1]);
    }

    public void setVillagerData(@NotNull VillagerData data) {
        super.metadata.setIndex(OFFSET, Metadata.VillagerData(
                data.type.ordinal(),
                data.profession.ordinal(),
                data.level.ordinal() + 1
        ));
    }

    public static class VillagerData {

        private Type type;
        private Profession profession;
        private Level level;

        public VillagerData(@NotNull Type type, @NotNull Profession profession, @NotNull Level level) {
            this.type = type;
            this.profession = profession;
            this.level = level;
        }

        @NotNull
        public Type getType() {
            return this.type;
        }

        public void setType(@NotNull Type type) {
            this.type = type;
        }

        @NotNull
        public Profession getProfession() {
            return this.profession;
        }

        public void setProfession(@NotNull Profession profession) {
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

    public enum Type {
        DESERT,
        JUNGLE,
        PLAINS,
        SAVANNA,
        SNOW,
        SWAMP,
        TAIGA;

        public final static Type[] VALUES = values();
    }

    public enum Profession {
        NONE,
        ARMORER,
        BUTCHER,
        CARTOGRAPHER,
        CLERIC,
        FARMER,
        FISHERMAN,
        FLETCHER,
        LEATHERWORKER,
        LIBRARIAN,
        NITWIT,
        UNEMPLOYED,
        MASON,
        SHEPHERD,
        TOOLSMITH,
        WEAPONSMITH;

        public final static Profession[] VALUES = values();
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
