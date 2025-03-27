package net.minestom.server.item.book;

import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record FilteredText<T>(@NotNull T text, @Nullable T filtered) {

    public static @NotNull NetworkBuffer.Type<FilteredText<String>> STRING_NETWORK_TYPE = createNetworkType(NetworkBuffer.STRING);
    public static @NotNull Codec<FilteredText<String>> STRING_CODEC = createCodec(Codec.STRING);

    public static @NotNull NetworkBuffer.Type<FilteredText<Component>> COMPONENT_NETWORK_TYPE = createNetworkType(NetworkBuffer.COMPONENT);
    public static @NotNull Codec<FilteredText<Component>> COMPONENT_CODEC = createCodec(Codec.COMPONENT);

    private static <T> NetworkBuffer.@NotNull Type<FilteredText<T>> createNetworkType(@NotNull NetworkBuffer.Type<T> inner) {
        return NetworkBufferTemplate.template(
                inner, FilteredText::text,
                inner.optional(), FilteredText::filtered,
                FilteredText::new);
    }

    private static <T> @NotNull Codec<FilteredText<T>> createCodec(@NotNull Codec<T> inner) {
        return StructCodec.struct(
                "raw", inner, FilteredText::text,
                "filtered", inner.optional(), FilteredText::filtered,
                FilteredText::new);
    }
}
