package net.minestom.server.network.packet.server.play.data;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;

public record WorldPos(String dimension, Point blockPosition) {
    public static final NetworkBuffer.Type<WorldPos> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.STRING, WorldPos::dimension,
            NetworkBuffer.BLOCK_POSITION, WorldPos::blockPosition,
            WorldPos::new);
    public static final Codec<WorldPos> CODEC = StructCodec.struct(
            "dimension", Codec.STRING, WorldPos::dimension,
            "pos", Codec.BLOCK_POSITION, WorldPos::blockPosition,
            WorldPos::new
    );

    public WorldPos withDimension(String dimension) {
        return new WorldPos(dimension, blockPosition);
    }

    public WorldPos withBlockPosition(Point blockPosition) {
        return new WorldPos(dimension, blockPosition);
    }
}