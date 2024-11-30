package net.minestom.server.item.component;

import net.kyori.adventure.util.RGBLike;
import net.minestom.server.color.Color;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.nbt.BinaryTagTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record CustomModelData(
        @NotNull List<Float> floats, @NotNull List<Boolean> flags,
        @NotNull List<String> strings, @NotNull List<RGBLike> colors
) {
    public static final NetworkBuffer.Type<CustomModelData> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.FLOAT.list(), CustomModelData::floats,
            NetworkBuffer.BOOLEAN.list(), CustomModelData::flags,
            NetworkBuffer.STRING.list(), CustomModelData::strings,
            Color.NETWORK_TYPE.list(), CustomModelData::colors,
            CustomModelData::new);
    public static final BinaryTagSerializer<CustomModelData> NBT_TYPE = BinaryTagTemplate.object(
            "floats", BinaryTagSerializer.FLOAT.list().optional(List.of()), CustomModelData::floats,
            "flags", BinaryTagSerializer.BOOLEAN.list().optional(List.of()), CustomModelData::flags,
            "strings", BinaryTagSerializer.STRING.list().optional(List.of()), CustomModelData::strings,
            "colors", Color.NBT_TYPE.list().optional(List.of()), CustomModelData::colors,
            CustomModelData::new);
}
