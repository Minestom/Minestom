package net.minestom.server.network.packet.server.play.data;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

public record WorldPos(@NotNull String dimension, @NotNull Point blockPosition) {
    public static final NetworkBuffer.Type<WorldPos> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.STRING, WorldPos::dimension,
            NetworkBuffer.BLOCK_POSITION, WorldPos::blockPosition,
            WorldPos::new
    );

    public static final BinaryTagSerializer<WorldPos> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
            tag -> new WorldPos(tag.getString("dimension"), BinaryTagSerializer.BLOCK_POSITION.read(tag.get("pos"))),
            pos -> CompoundBinaryTag.builder()
                    .putString("dimension", pos.dimension)
                    .put("pos", BinaryTagSerializer.BLOCK_POSITION.write(pos.blockPosition))
                    .build()
    );

    public @NotNull WorldPos withDimension(@NotNull String dimension) {
        return new WorldPos(dimension, blockPosition);
    }

    public @NotNull WorldPos withBlockPosition(@NotNull Point blockPosition) {
        return new WorldPos(dimension, blockPosition);
    }
}