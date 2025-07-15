package net.minestom.server.item.book;

import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import org.jspecify.annotations.Nullable;

public record FilteredText<T>(T text, @Nullable T filtered) {

    public static NetworkBuffer.Type<FilteredText<String>> STRING_NETWORK_TYPE = createNetworkType(NetworkBuffer.STRING);
    public static Codec<FilteredText<String>> STRING_CODEC = createCodec(Codec.STRING);

    public static NetworkBuffer.Type<FilteredText<Component>> COMPONENT_NETWORK_TYPE = createNetworkType(NetworkBuffer.COMPONENT);
    public static Codec<FilteredText<Component>> COMPONENT_CODEC = createCodec(Codec.COMPONENT);

    private static <T> NetworkBuffer.Type<FilteredText<T>> createNetworkType(NetworkBuffer.Type<T> inner) {
        return NetworkBufferTemplate.template(
                inner, FilteredText::text,
                inner.optional(), FilteredText::filtered,
                FilteredText::new);
    }

    private static <T> Codec<FilteredText<T>> createCodec(Codec<T> inner) {
        return StructCodec.struct(
                "raw", inner, FilteredText::text,
                "filtered", inner.optional(), FilteredText::filtered,
                FilteredText::new);
    }
}
