package net.minestom.server.world;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * https://minecraft.gamepedia.com/Custom_dimension
 */
@Data
@Builder(builderMethodName = "hiddenBuilder", access = AccessLevel.PUBLIC)
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
            .fixedTime(Optional.empty())
            .ambientLight(0.0f)
            .logicalHeight(256)
            .infiniburn(NamespaceID.from("minecraft:infiniburn_overworld"))
            .build();

    private final int id = idCounter.getAndIncrement();
    @NotNull
    private final NamespaceID name;
    private final boolean natural;
    private final float ambientLight;
    private final boolean ceilingEnabled;
    private final boolean skylightEnabled;
    @Builder.Default
    private final Optional<Long> fixedTime = Optional.empty();
    private final boolean raidCapable;
    private final boolean respawnAnchorSafe;
    private final boolean ultrawarm;
    @Builder.Default
    private final boolean bedSafe = true;
    private final boolean piglinSafe;
    @Builder.Default
    private final int logicalHeight = 256;
    @Builder.Default
    private final int coordinateScale = 1;
    @Builder.Default
    private final NamespaceID infiniburn = NamespaceID.from("minecraft:infiniburn_overworld");

    public static DimensionTypeBuilder builder(NamespaceID name) {
        return hiddenBuilder().name(name);
    }

    public NBTCompound toIndexedNBT() {
        NBTCompound nbt = new NBTCompound();
        NBTCompound element = toNBT();
        nbt.setString("name", name.toString());
        nbt.setInt("id", id);
        nbt.set("element", element);
        return nbt;
    }

    public NBTCompound toNBT() {
        NBTCompound nbt = new NBTCompound()
                .setFloat("ambient_light", ambientLight)
                .setString("infiniburn", infiniburn.toString())
                .setByte("natural", (byte) (natural ? 0x01 : 0x00))
                .setByte("has_ceiling", (byte) (ceilingEnabled ? 0x01 : 0x00))
                .setByte("has_skylight", (byte) (skylightEnabled ? 0x01 : 0x00))
                .setByte("ultrawarm", (byte) (ultrawarm ? 0x01 : 0x00))
                .setByte("has_raids", (byte) (raidCapable ? 0x01 : 0x00))
                .setByte("respawn_anchor_works", (byte) (respawnAnchorSafe ? 0x01 : 0x00))
                .setByte("bed_works", (byte) (bedSafe ? 0x01 : 0x00))
                .setByte("piglin_safe", (byte) (piglinSafe ? 0x01 : 0x00))
                .setInt("logical_height", logicalHeight)
                .setInt("coordinate_scale", coordinateScale)
                .setString("name", name.toString());
        fixedTime.ifPresent(time -> nbt.setLong("fixed_time", time));
        return nbt;
    }

    @Override
    public String toString() {
        return name.toString();
    }

    public static class DimensionTypeBuilder {
    }

}
