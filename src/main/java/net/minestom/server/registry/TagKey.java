package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.codec.Codec;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.ApiStatus;

public sealed interface TagKey<T> extends Keyed permits TagKeyImpl {
    static <T> Codec<TagKey<T>> codec(Registries.Selector<T> selector) {
        return new RegistryCodecs.TagKeyImpl<>(selector, false);
    }

    /**
     * Creates a codec encoding the tag key in the hashed {@code #namespace:value} form.
     *
     * @param selector selects the owning registry
     * @param <T>      the registry entry type
     * @return the hashed tag key codec
     * @deprecated no longer used by any component; vanilla encodes tag valued components as holder
     * sets, see {@link RegistryTag#codec(Registries.Selector)}
     */
    @Deprecated(forRemoval = true)
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
     * Creates a tag key from an unhashed key. Should not be used externally.
     */
    @ApiStatus.Internal
    static <T> TagKey<T> unsafeOf(@KeyPattern String key) {
        return unsafeOf(Key.key(key));
    }

    /**
     * Creates a tag key from an unhashed key. Should not be used externally.
     */
    @ApiStatus.Internal
    static <T> TagKey<T> unsafeOf(Key key) {
        return new TagKeyImpl<>(key);
    }

    default String hashedKey() {
        return "#" + key().asString();
    }


}
