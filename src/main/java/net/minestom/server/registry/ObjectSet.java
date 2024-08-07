package net.minestom.server.registry;

import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.key.Namespaced;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;

/**
 * A set of some namespaced objects. May contain a single element, multiple elements, or a single tag (which itself contains multiple elements).
 */
public sealed interface ObjectSet permits ObjectSetImpl {

    static @NotNull ObjectSet empty() {
        return ObjectSetImpl.Empty.INSTANCE;
    }

    static @NotNull ObjectSet of(@NotNull Collection<NamespaceID> entries) {
        return new ObjectSetImpl.Entries(Set.copyOf(entries));
    }

    static @NotNull ObjectSet of(@NotNull Tag tag) {
        return new ObjectSetImpl.Tag(tag);
    }

    static @NotNull BinaryTagSerializer<ObjectSet> nbtType(@NotNull Tag.BasicType tagType) {
        return new ObjectSetImpl.NbtType(tagType);
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
        return contains(object.namespace());
    }

    default boolean contains(@NotNull DynamicRegistry.Key<?> key) {
        return contains(key.namespace());
    }

    boolean contains(@NotNull NamespaceID namespace);

}
