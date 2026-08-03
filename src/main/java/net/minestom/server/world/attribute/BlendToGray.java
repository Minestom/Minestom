package net.minestom.server.world.attribute;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;

public value record BlendToGray(float brightness, float factor) {
    public static final Codec<BlendToGray> CODEC = StructCodec.struct(
            "brightness", Codec.FLOAT, BlendToGray::brightness,
            "factor", Codec.FLOAT, BlendToGray::factor,
            BlendToGray::new);
}
