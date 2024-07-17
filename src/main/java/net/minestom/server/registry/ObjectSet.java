package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * A set of some keyed objects. May contain a single element, multiple elements, or a single tag (which itself contains multiple elements).
 */
public sealed interface ObjectSet permits ObjectSetImpl {

    static @NotNull ObjectSet empty() {
        return ObjectSetImpl.Empty.INSTANCE;
    }

    static @NotNull ObjectSet of(@NotNull Collection<Key> entries) {
        return new ObjectSetImpl.Entries(List.copyOf(entries));
    }

    static @NotNull ObjectSet of(@NotNull Tag tag) {
        return new ObjectSetImpl.Tag(tag);
    }

    static NetworkBuffer.@NotNull Type<ObjectSet> networkType(@NotNull Tag.BasicType tagType) {
        return new ObjectSetImpl.NetworkType(tagType);
    }

    static @NotNull Codec<ObjectSet> codec(@NotNull Tag.BasicType tagType) {
        return new ObjectSetImpl.CodecImpl(tagType);
    }

    /**
     * <p>Check if this set contains the given object, tested against its namespace id.</p>
     *
     * <p>Present for compatibility with non-dynamic registries. Will be removed in the future.</p>
     *
     * @param object The object to check for.
     * @return True if this set contains the object, false otherwise.
     */
    default boolean contains(@NotNull StaticProtocolObject object) {
        return contains(object.key());
    }

    default boolean contains(@NotNull DynamicRegistry.Key<?> key) {
        return contains(key.key());
    }

    boolean contains(@NotNull Key namespace);

}
