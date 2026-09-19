package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.codec.Codec;
import net.minestom.server.network.NetworkBuffer;

public sealed interface TagKey<T> extends Keyed permits TagKeyImpl {
    static <T> Codec<TagKey<T>> codec(Registries.Selector<T> selector) {
        return new RegistryCodecs.TagKeyImpl<>(selector);
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
     * Creates a tag key from an unhashed string. The key syntax is validated, tag existence is not. A tag key is only
     * a typed name. Looking up a tag that does not exist returns {@code null}.
     *
     * @param key the tag key without a leading {@code #}
     * @param <T> the registry entry type
     * @return the tag key
     * @throws IllegalArgumentException if {@code key} is invalid
     * @throws NullPointerException     if {@code key} is {@code null}
     */
    static <T> TagKey<T> of(@KeyPattern String key) {
        return of(Key.key(key));
    }

    /**
     * Creates a tag key from an unhashed key without checking that the tag exists. See {@link #of(String)}.
     *
     * @param key the tag key
     * @param <T> the registry entry type
     * @return the tag key
     * @throws NullPointerException if {@code key} is {@code null}
     */
    static <T> TagKey<T> of(Key key) {
        return new TagKeyImpl<>(key);
    }

    default String hashedKey() {
        return "#" + key().asString();
    }


}
