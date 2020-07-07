package net.minestom.server.world;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import net.minestom.server.utils.NamespaceID;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Optional;

/**
 * https://minecraft.gamepedia.com/Custom_dimension
 */
@Data
@Builder(builderMethodName = "hiddenBuilder", access = AccessLevel.PRIVATE)
public class Dimension {

    public static final Dimension OVERWORLD = Dimension.builder(NamespaceID.from("minecraft:overworld"))
            .ultrawarm(false)
            .natural(true)
            .shrunk(false)
            .piglinSafe(false)
            .respawnAnchorSafe(false)
            .bedSafe(true)
            .raidCapable(true)
            .skylightEnabled(true)
            .ceilingEnabled(false)
            .fixedTime(Optional.empty())
            .ambientLight(0.0f)
            .logicalHeight(256)
            .infiniburn(NamespaceID.from("minecraft:infiniburn_overworld"))
            .build();

    public static final Dimension NETHER = Dimension.builder(NamespaceID.from("minecraft:the_nether"))
            .ultrawarm(true)
            .natural(false)
            .shrunk(true)
            .piglinSafe(true)
            .respawnAnchorSafe(true)
            .bedSafe(false)
            .raidCapable(false)
            .skylightEnabled(false)
            .ceilingEnabled(true)
            .fixedTime(Optional.of(18000L))
            .ambientLight(0.1f)
            .logicalHeight(128)
            .infiniburn(NamespaceID.from("minecraft:infiniburn_nether"))
            .build();

    public static final Dimension END = Dimension.builder(NamespaceID.from("minecraft:the_end"))
            .ultrawarm(false)
            .natural(false)
            .shrunk(false)
            .piglinSafe(false)
            .respawnAnchorSafe(false)
            .bedSafe(false)
            .raidCapable(true)
            .skylightEnabled(false)
            .ceilingEnabled(false)
            .fixedTime(Optional.of(6000L))
            .ambientLight(0.0f)
            .logicalHeight(256)
            .infiniburn(NamespaceID.from("minecraft:infiniburn_end"))
            .build();

    private final NamespaceID name;
    private final boolean natural;
    private final float ambientLight;
    private final boolean ceilingEnabled;
    private final boolean skylightEnabled;
    @Builder.Default private final Optional<Long> fixedTime = Optional.empty();
    private final boolean shrunk;
    private final boolean raidCapable;
    private final boolean respawnAnchorSafe;
    private final boolean ultrawarm;
    @Builder.Default private final boolean bedSafe = true;
    private final boolean piglinSafe;
    @Builder.Default private final int logicalHeight = 256;
    @Builder.Default private final NamespaceID infiniburn = NamespaceID.from("minecraft:infiniburn_overworld");

    public NBTCompound toNBT() {
        NBTCompound nbt = new NBTCompound()
                .setString("name", name.toString())
                .setFloat("ambient_light", ambientLight)
                .setString("infiniburn", infiniburn.toString())
                .setByte("natural", (byte) (natural ? 0x01 : 0x00))
                .setByte("has_ceiling", (byte) (ceilingEnabled ? 0x01 : 0x00))
                .setByte("has_skylight", (byte) (skylightEnabled ? 0x01 : 0x00))
                .setByte("shrunk", (byte) (shrunk ? 0x01 : 0x00))
                .setByte("ultrawarm", (byte) (ultrawarm ? 0x01 : 0x00))
                .setByte("has_raids", (byte) (raidCapable ? 0x01 : 0x00))
                .setByte("respawn_anchor_works", (byte) (respawnAnchorSafe ? 0x01 : 0x00))
                .setByte("bed_works", (byte) (bedSafe ? 0x01 : 0x00))
                .setByte("piglin_safe", (byte) (piglinSafe ? 0x01 : 0x00))
                .setInt("logical_height", logicalHeight)
        ;
        fixedTime.ifPresent(time -> nbt.setLong("fixed_time", time));
        return nbt;
    }

    @Override
    public String toString() {
        return name.toString();
    }

    public static DimensionBuilder builder(NamespaceID name) {
        return hiddenBuilder().name(name);
    }

}
