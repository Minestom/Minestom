package net.minestom.server.world;

import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.mcdata.SizesKt;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * https://minecraft.gamepedia.com/Custom_dimension
 */
public class DimensionType {

    private static final AtomicInteger idCounter = new AtomicInteger(0);

    public static final DimensionType OVERWORLD = DimensionType.builder(NamespaceID.from("minecraft:overworld"))
            .ultrawarm(false)
            .natural(true)
            .piglinSafe(false)
            .respawnAnchorSafe(false)
            .bedSafe(true)
            .raidCapable(true)
            .skylightEnabled(true)
            .ceilingEnabled(false)
            .fixedTime(null)
            .ambientLight(0.0f)
            .height(384)
            .minY(-64)
            .logicalHeight(384)
            .infiniburn(NamespaceID.from("minecraft:infiniburn_overworld"))
            .build();

    private final int id = idCounter.getAndIncrement();

    protected volatile boolean registered;

    private final NamespaceID name;
    private final boolean natural;
    private final float ambientLight;
    private final boolean ceilingEnabled;
    private final boolean skylightEnabled;

    @Nullable
    private final Long fixedTime;

    private final boolean raidCapable;
    private final boolean respawnAnchorSafe;
    private final boolean ultrawarm;
    private final boolean bedSafe;
    private final String effects;
    private final boolean piglinSafe;
    private final int minY;
    private final int height;
    private final int logicalHeight;
    private final int coordinateScale;
    private final NamespaceID infiniburn;

    DimensionType(NamespaceID name, boolean natural, float ambientLight, boolean ceilingEnabled,
                  boolean skylightEnabled, @Nullable Long fixedTime, boolean raidCapable,
                  boolean respawnAnchorSafe, boolean ultrawarm, boolean bedSafe, String effects, boolean piglinSafe,
                  int minY, int height, int logicalHeight, int coordinateScale, NamespaceID infiniburn) {
        this.name = name;
        this.natural = natural;
        this.ambientLight = ambientLight;
        this.ceilingEnabled = ceilingEnabled;
        this.skylightEnabled = skylightEnabled;
        this.fixedTime = fixedTime;
        this.raidCapable = raidCapable;
        this.respawnAnchorSafe = respawnAnchorSafe;
        this.ultrawarm = ultrawarm;
        this.bedSafe = bedSafe;
        this.effects = effects;
        this.piglinSafe = piglinSafe;
        this.minY = minY;
        this.height = height;
        this.logicalHeight = logicalHeight;
        this.coordinateScale = coordinateScale;
        this.infiniburn = infiniburn;
    }

    public static DimensionTypeBuilder builder(NamespaceID name) {
        return hiddenBuilder().name(name);
    }

    public static DimensionTypeBuilder hiddenBuilder() {
        return new DimensionTypeBuilder();
    }

    public static DimensionType fromNBT(NBTCompound nbt) {
        return DimensionType.builder(NamespaceID.from(nbt.getString("name")))
                .ambientLight(nbt.getFloat("ambient_light"))
                .infiniburn(NamespaceID.from(nbt.getString("infiniburn")))
                .natural(nbt.getByte("natural") != 0)
                .ceilingEnabled(nbt.getByte("has_ceiling") != 0)
                .skylightEnabled(nbt.getByte("has_skylight") != 0)
                .ultrawarm(nbt.getByte("ultrawarm") != 0)
                .raidCapable(nbt.getByte("has_raids") != 0)
                .respawnAnchorSafe(nbt.getByte("respawn_anchor_works") != 0)
                .bedSafe(nbt.getByte("bed_works") != 0)
                .effects(nbt.getString("effects"))
                .piglinSafe(nbt.getByte("piglin_safe") != 0)
                .logicalHeight(nbt.getInt("logical_height"))
                .coordinateScale(nbt.getInt("coordinate_scale"))
                .build();
    }

    @NotNull
    public NBTCompound toIndexedNBT() {
        return NBT.Compound(Map.of(
                "name", NBT.String(name.toString()),
                "id", NBT.Int(id),
                "element", toNBT()));
    }

    @NotNull
    public NBTCompound toNBT() {
        return NBT.Compound(nbt -> {
            nbt.setFloat("ambient_light", ambientLight);
            nbt.setString("infiniburn", infiniburn.toString());
            nbt.setByte("natural", (byte) (natural ? 0x01 : 0x00));
            nbt.setByte("has_ceiling", (byte) (ceilingEnabled ? 0x01 : 0x00));
            nbt.setByte("has_skylight", (byte) (skylightEnabled ? 0x01 : 0x00));
            nbt.setByte("ultrawarm", (byte) (ultrawarm ? 0x01 : 0x00));
            nbt.setByte("has_raids", (byte) (raidCapable ? 0x01 : 0x00));
            nbt.setByte("respawn_anchor_works", (byte) (respawnAnchorSafe ? 0x01 : 0x00));
            nbt.setByte("bed_works", (byte) (bedSafe ? 0x01 : 0x00));
            nbt.setString("effects", effects);
            nbt.setByte("piglin_safe", (byte) (piglinSafe ? 0x01 : 0x00));
            nbt.setInt("min_y", minY);
            nbt.setInt("height", height);
            nbt.setInt("logical_height", logicalHeight);
            nbt.setInt("coordinate_scale", coordinateScale);
            nbt.setString("name", name.toString());
            if (fixedTime != null) nbt.setLong("fixed_time", fixedTime);
        });
    }

    @Override
    public String toString() {
        return name.toString();
    }

    public int getId() {
        return this.id;
    }

    public boolean isRegistered() {
        return registered;
    }

    public NamespaceID getName() {
        return this.name;
    }

    public boolean isNatural() {
        return this.natural;
    }

    public float getAmbientLight() {
        return this.ambientLight;
    }

    public boolean isCeilingEnabled() {
        return this.ceilingEnabled;
    }

    public boolean isSkylightEnabled() {
        return this.skylightEnabled;
    }

    @Nullable
    public Long getFixedTime() {
        return this.fixedTime;
    }

    public boolean isRaidCapable() {
        return this.raidCapable;
    }

    public boolean isRespawnAnchorSafe() {
        return this.respawnAnchorSafe;
    }

    public boolean isUltrawarm() {
        return this.ultrawarm;
    }

    public boolean isBedSafe() {
        return this.bedSafe;
    }

    public String getEffects() {
        return effects;
    }

    public boolean isPiglinSafe() {
        return this.piglinSafe;
    }

    public int getMinY() {
        return minY;
    }

    public int getHeight() {
        return height;
    }

    public int getMaxY() {
        return getMinY() + getHeight();
    }

    public int getLogicalHeight() {
        return this.logicalHeight;
    }

    public int getCoordinateScale() {
        return this.coordinateScale;
    }

    public NamespaceID getInfiniburn() {
        return this.infiniburn;
    }

    public int getTotalHeight() {
        return minY + height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DimensionType that = (DimensionType) o;
        return id == that.id &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    public static class DimensionTypeBuilder {
        private NamespaceID name;
        private boolean natural;
        private float ambientLight;
        private boolean ceilingEnabled;
        private boolean skylightEnabled;

        @Nullable
        private Long fixedTime = null;
        private boolean raidCapable;
        private boolean respawnAnchorSafe;
        private boolean ultrawarm;
        private boolean bedSafe = true;
        private String effects = "minecraft:overworld";
        private boolean piglinSafe = false;
        private int minY = SizesKt.getVanillaMinY();
        private int logicalHeight = SizesKt.getVanillaMaxY() - SizesKt.getVanillaMinY() + 1;
        private int height = SizesKt.getVanillaMaxY() - SizesKt.getVanillaMinY() + 1;
        private int coordinateScale = 1;
        private NamespaceID infiniburn = NamespaceID.from("minecraft:infiniburn_overworld");

        DimensionTypeBuilder() {
        }

        public DimensionType.DimensionTypeBuilder name(NamespaceID name) {
            this.name = name;
            return this;
        }

        public DimensionType.DimensionTypeBuilder natural(boolean natural) {
            this.natural = natural;
            return this;
        }

        public DimensionType.DimensionTypeBuilder ambientLight(float ambientLight) {
            this.ambientLight = ambientLight;
            return this;
        }

        public DimensionType.DimensionTypeBuilder ceilingEnabled(boolean ceilingEnabled) {
            this.ceilingEnabled = ceilingEnabled;
            return this;
        }

        public DimensionType.DimensionTypeBuilder skylightEnabled(boolean skylightEnabled) {
            this.skylightEnabled = skylightEnabled;
            return this;
        }

        public DimensionType.DimensionTypeBuilder fixedTime(Long fixedTime) {
            this.fixedTime = fixedTime;
            return this;
        }

        public DimensionType.DimensionTypeBuilder raidCapable(boolean raidCapable) {
            this.raidCapable = raidCapable;
            return this;
        }

        public DimensionType.DimensionTypeBuilder respawnAnchorSafe(boolean respawnAnchorSafe) {
            this.respawnAnchorSafe = respawnAnchorSafe;
            return this;
        }

        public DimensionType.DimensionTypeBuilder ultrawarm(boolean ultrawarm) {
            this.ultrawarm = ultrawarm;
            return this;
        }

        public DimensionType.DimensionTypeBuilder bedSafe(boolean bedSafe) {
            this.bedSafe = bedSafe;
            return this;
        }

        public DimensionType.DimensionTypeBuilder effects(String effects) {
            this.effects = effects;
            return this;
        }

        public DimensionType.DimensionTypeBuilder piglinSafe(boolean piglinSafe) {
            this.piglinSafe = piglinSafe;
            return this;
        }

        public DimensionType.DimensionTypeBuilder minY(int minY) {
            this.minY = minY;
            return this;
        }

        public DimensionType.DimensionTypeBuilder height(int height) {
            this.height = height;
            return this;
        }

        public DimensionType.DimensionTypeBuilder logicalHeight(int logicalHeight) {
            this.logicalHeight = logicalHeight;
            return this;
        }

        public DimensionType.DimensionTypeBuilder coordinateScale(int coordinateScale) {
            this.coordinateScale = coordinateScale;
            return this;
        }

        public DimensionType.DimensionTypeBuilder infiniburn(NamespaceID infiniburn) {
            this.infiniburn = infiniburn;
            return this;
        }

        public DimensionType build() {
            return new DimensionType(name, natural, ambientLight, ceilingEnabled, skylightEnabled,
                    fixedTime, raidCapable, respawnAnchorSafe, ultrawarm, bedSafe, effects,
                    piglinSafe, minY, height, logicalHeight, coordinateScale, infiniburn);
        }
    }
}
