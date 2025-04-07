package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.network.packet.server.common.TagsPacket;
import net.minestom.server.utils.collection.ObjectArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class StaticRegistry<T extends StaticProtocolObject> implements Registry<T> {
    private final String id;
    private final Map<String, T> namespaces;
    private final ObjectArray<T> ids;
    private final List<T> values;

    private final Map<String, ObjectSetImpl.TagV2<T>> tags;

    StaticRegistry(
            @NotNull String id,
            @NotNull Map<String, T> namespaces,
            @NotNull ObjectArray<T> ids,
            @NotNull Map<String, ObjectSetImpl.TagV2<T>> tags
    ) {
        this.id = id;
        this.namespaces = Map.copyOf(namespaces);
        this.ids = ids;
        this.ids.trim();

        //noinspection unchecked
        this.values = List.of(ids.arrayCopy((Class<T>) StaticProtocolObject.class));

        this.tags = Map.copyOf(tags);
    }

    @Override
    public @NotNull String id() {
        return this.id;
    }

    @Override
    public @Nullable T get(int id) {
        return ids.get(id);
    }

    @Override
    public @Nullable T get(@NotNull Key key) {
        return namespaces.get(key.asString());
    }

    @Override
    public DynamicRegistry.@Nullable Key<T> getKey(int id) {
        final T value = ids.get(id);
        return value == null ? null : DynamicRegistry.Key.of(value.key());
    }

    @Override
    public DynamicRegistry.Key<T> getKey(@NotNull T value) {
        return DynamicRegistry.Key.of(value.key());
    }

    @Override
    public @Nullable Key getName(int id) {
        final T value = ids.get(id);
        return value == null ? null : value.key();
    }

    @Override
    public @NotNull DataPack getPack(int id) {
        return DataPack.MINECRAFT_CORE;
    }

    @Override
    public @NotNull DataPack getPack(DynamicRegistry.@NotNull Key<T> key) {
        return DataPack.MINECRAFT_CORE;
    }

    @Override
    public int getId(@NotNull Key id) {
        final T value = get(id);
        return value == null ? -1 : value.id();
    }

    @Override
    public @Nullable ObjectSet<T> getTag(DynamicRegistry.@NotNull Key<T> key) {
        return tags.get(key.key().asString());
    }

    @Override
    public @NotNull List<T> values() {
        return values;
    }

    @Override
    public @NotNull Collection<ObjectSet<T>> tags() {
        //noinspection unchecked
        return (Collection<ObjectSet<T>>) (Object) tags.values();
    }

    @Override
    public TagsPacket.@NotNull Registry tagRegistry() {
        throw new UnsupportedOperationException("todo");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StaticRegistry<?> registry)) return false;
        return Objects.equals(id, registry.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
