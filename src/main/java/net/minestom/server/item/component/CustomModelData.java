package net.minestom.server.item.component;

import net.kyori.adventure.util.RGBLike;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.color.Color;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;

import java.util.List;

public record CustomModelData(
        List<Float> floats, List<Boolean> flags,
        List<String> strings, List<RGBLike> colors
) {
    public static final NetworkBuffer.Type<CustomModelData> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.FLOAT.list(), CustomModelData::floats,
            NetworkBuffer.BOOLEAN.list(), CustomModelData::flags,
            NetworkBuffer.STRING.list(), CustomModelData::strings,
            Color.NETWORK_TYPE.list(), CustomModelData::colors,
            CustomModelData::new);
    public static final Codec<CustomModelData> CODEC = StructCodec.struct(
            "floats", Codec.FLOAT.list().optional(List.of()), CustomModelData::floats,
            "flags", Codec.BOOLEAN.list().optional(List.of()), CustomModelData::flags,
            "strings", Codec.STRING.list().optional(List.of()), CustomModelData::strings,
            "colors", Color.CODEC.list().optional(List.of()), CustomModelData::colors,
            CustomModelData::new);
}
