package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.codec.Codec;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.ApiStatus;

public sealed interface TagKey<T> extends Keyed permits TagKeyImpl {
    static <T> Codec<TagKey<T>> codec(Registries.Selector<T> selector) {
        return new RegistryCodecs.TagKeyImpl<>(selector, false);
    }

    static <T> Codec<TagKey<T>> hashCodec(Registries.Selector<T> selector) {
        return new RegistryCodecs.TagKeyImpl<>(selector, true);
    }

    static <T> NetworkBuffer.Type<TagKey<T>> networkType(Registries.Selector<T> selector) {
        return NetworkBuffer.KEY.transform(TagKeyImpl::new, TagKey::key);
    }

    static <T> TagKey<T> ofHash(String hashedKey) {
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
    static <T> TagKey<T> unsafeOf(Key key) {
        return new TagKeyImpl<>(key);
    }

    default String hashedKey() {
        return "#" + key().asString();
    }


}
