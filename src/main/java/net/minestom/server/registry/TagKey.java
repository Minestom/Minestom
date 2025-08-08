package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.codec.Codec;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public sealed interface TagKey<T> extends Keyed permits TagKeyImpl {
    static <T> @NotNull Codec<TagKey<T>> codec(@NotNull Registries.Selector<T> selector) {
        return new RegistryCodecs.TagKeyImpl<>(selector, false);
    }

    static <T> @NotNull Codec<TagKey<T>> hashCodec(@NotNull Registries.Selector<T> selector) {
        return new RegistryCodecs.TagKeyImpl<>(selector, true);
    }

    static <T> NetworkBuffer.@NotNull Type<TagKey<T>> networkType(@NotNull Registries.Selector<T> selector) {
        return NetworkBuffer.KEY.transform(TagKeyImpl::new, TagKey::key);
    }

    static <T> @NotNull TagKey<T> ofHash(@NotNull String hashedKey) {
        if (!hashedKey.startsWith("#"))
            throw new IllegalArgumentException("Hashed key must start with '#': " + hashedKey);
        return new TagKeyImpl<>(Key.key(hashedKey.substring(1)));
    }

    /**
     * Creates a new {@link TagKey} from the given raw string. Should not be used externally.
     * Use {@link TagKey#ofHash(String)} instead.
     *
     * @param key the key to create the TagKey from
     * @return a new TagKey instance
     * @param <T> the type of the registry entry
     */
    @ApiStatus.Internal
    static <T> @NotNull TagKey<T> unsafeOf(@NotNull Key key) {
        return new TagKeyImpl<>(key);
    }

    default @NotNull String hashedKey() {
        return "#" + key().asString();
    }


}
