package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.codec.Codec;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * A set of some keyed objects. May contain a single element, multiple elements, or a single tag (which itself contains multiple elements).
 */
public sealed interface ObjectSet<T extends Keyed> permits ObjectSetImpl {

    static <T extends Keyed> @NotNull ObjectSet<T> empty() {
        // noinspection unchecked
        return (ObjectSet<T>) ObjectSetImpl.Empty.INSTANCE;
    }

    static <T extends Keyed> @NotNull ObjectSet<T> of(@NotNull Collection<Key> entries) {
        return new ObjectSetImpl.Entries<>(List.copyOf(entries));
    }

    static <T extends Keyed> @NotNull ObjectSet<T> of(@NotNull Tag tag) {
        return new ObjectSetImpl.Tag<>(tag);
    }

    static <T extends Keyed> NetworkBuffer.@NotNull Type<ObjectSet<T>> networkType(@NotNull Tag.BasicType tagType) {
        return new ObjectSetImpl.NetworkType<>(tagType);
    }

    static <T extends Keyed> @NotNull Codec<ObjectSet<T>> codec(@NotNull Tag.BasicType tagType) {
        return new ObjectSetImpl.CodecImpl<>(tagType);
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

    default boolean contains(@NotNull DynamicRegistry.Key<T> key) {
        return contains(key.key());
    }

    boolean contains(@NotNull Key namespace);

}
