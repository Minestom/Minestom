package net.minestom.server.item.book;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record FilteredText<T>(@NotNull T text, @Nullable T filtered) {

    public static @NotNull NetworkBuffer.Type<FilteredText<String>> STRING_NETWORK_TYPE = createNetworkType(NetworkBuffer.STRING);
    public static @NotNull BinaryTagSerializer<FilteredText<String>> STRING_NBT_TYPE = createNbtType(BinaryTagSerializer.STRING);

    public static @NotNull NetworkBuffer.Type<FilteredText<Component>> COMPONENT_NETWORK_TYPE = createNetworkType(NetworkBuffer.COMPONENT);
    public static @NotNull BinaryTagSerializer<FilteredText<Component>> COMPONENT_NBT_TYPE = createNbtType(BinaryTagSerializer.JSON_COMPONENT);

    private static <T> NetworkBuffer.@NotNull Type<FilteredText<T>> createNetworkType(@NotNull NetworkBuffer.Type<T> inner) {
        return new NetworkBuffer.Type<>() {
            @Override
            public void write(@NotNull NetworkBuffer buffer, FilteredText<T> value) {
                buffer.write(inner, value.text);
                buffer.write(inner.optional(), value.filtered);
            }

            @Override
            public FilteredText<T> read(@NotNull NetworkBuffer buffer) {
                return new FilteredText<>(buffer.read(inner), buffer.read(inner.optional()));
            }
        };
    }

    private static <T> @NotNull BinaryTagSerializer<FilteredText<T>> createNbtType(@NotNull BinaryTagSerializer<T> inner) {
        return new BinaryTagSerializer<>() {
            @Override
            public @NotNull BinaryTag write(@NotNull FilteredText<T> value) {
                CompoundBinaryTag.Builder builder = CompoundBinaryTag.builder();
                builder.put("raw", inner.write(value.text));
                if (value.filtered != null) {
                    builder.put("filtered", inner.write(value.filtered));
                }
                return builder.build();
            }

            @Override
            public @NotNull FilteredText<T> read(@NotNull BinaryTag tag) {
                if (tag instanceof CompoundBinaryTag compound) {
                    BinaryTag textTag = compound.get("raw");
                    if (textTag != null) {
                        BinaryTag filteredTag = compound.get("filtered");
                        T filtered = filteredTag == null ? null : inner.read(filteredTag);
                        return new FilteredText<>(inner.read(textTag), filtered);
                    }
                }
                return new FilteredText<>(inner.read(tag), null);
            }
        };
    }
}
