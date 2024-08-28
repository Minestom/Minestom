package net.minestom.server.item.component;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.play.data.WorldPos;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record LodestoneTracker(@Nullable WorldPos target, boolean tracked) {

    public static final NetworkBuffer.Type<LodestoneTracker> NETWORK_TYPE = NetworkBufferTemplate.template(
            WorldPos.NETWORK_TYPE.optional(), LodestoneTracker::target,
            NetworkBuffer.BOOLEAN, LodestoneTracker::tracked,
            LodestoneTracker::new
    );

    public static final BinaryTagSerializer<LodestoneTracker> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
            tag -> new LodestoneTracker(
                    WorldPos.NBT_TYPE.read(tag.get("target")),
                    tag.getBoolean("tracked")),
            value -> CompoundBinaryTag.builder()
                    .put("target", WorldPos.NBT_TYPE.write(value.target))
                    .putBoolean("tracked", value.tracked)
                    .build()
    );

    public LodestoneTracker(@NotNull String dimension, @NotNull Point blockPosition, boolean tracked) {
        this(new WorldPos(dimension, blockPosition), tracked);
    }

    public @NotNull LodestoneTracker withTarget(@Nullable WorldPos target) {
        return new LodestoneTracker(target, tracked);
    }

    public @NotNull LodestoneTracker withTracked(boolean tracked) {
        return new LodestoneTracker(target, tracked);
    }

}
