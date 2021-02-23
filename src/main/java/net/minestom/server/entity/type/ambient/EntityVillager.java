package net.minestom.server.entity.type.ambient;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.type.AgeableCreature;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by k.shandurenko on 23.02.2021
 */
public class EntityVillager extends EntityAbstractVillager {

    public EntityVillager(@NotNull Position spawnPosition) {
        this(EntityType.VILLAGER, spawnPosition);
    }

    public EntityVillager(@NotNull Position spawnPosition, @Nullable Instance instance) {
        this(EntityType.VILLAGER, spawnPosition, instance);
    }

    EntityVillager(@NotNull EntityType entityType, @NotNull Position spawnPosition) {
        super(entityType, spawnPosition);
        setBoundingBox(.6D, 1.95D, .6D);
    }

    EntityVillager(@NotNull EntityType entityType, @NotNull Position spawnPosition, @Nullable Instance instance) {
        super(entityType, spawnPosition, instance);
        setBoundingBox(.6D, 1.95D, .6D);
    }

    public VillagerData getVillagerData() {
        int[] data = this.metadata.getIndex((byte) 17, null);
        if (data == null) {
            return new VillagerData(Type.PLAINS, Profession.NONE, Level.NOVICE);
        }
        return new VillagerData(Type.VALUES[data[0]], Profession.VALUES[data[1]], Level.VALUES[data[2] - 1]);
    }

    public void setVillagerData(VillagerData data) {
        this.metadata.setIndex((byte) 17, Metadata.VillagerData(
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
