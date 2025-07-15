package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.network.packet.server.common.TagsPacket;
import net.minestom.server.utils.collection.ObjectArray;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A registry for holding static vanilla registry data. Not generally user modifiable, always immutable.
 */
@ApiStatus.Internal
final class StaticRegistry<T extends StaticProtocolObject<T>> implements Registry<T> {
    private final Key key;
    private final Map<Key, T> keyToValue;
    private final Map<T, RegistryKey<T>> valueToKey;
    private final List<T> idToValue;

    private final Map<TagKey<T>, RegistryTagImpl.Backed<T>> tags;

    StaticRegistry(
            Key key,
            Map<Key, T> namespaces,
            ObjectArray<T> ids,
            Map<TagKey<T>, RegistryTagImpl.Backed<T>> tags
    ) {
        this.key = key;
        this.keyToValue = Map.copyOf(namespaces);
        var valueToKey = new HashMap<T, RegistryKey<T>>(namespaces.size());
        for (var entry : namespaces.entrySet())
            valueToKey.put(entry.getValue(), new RegistryKeyImpl<>(entry.getKey()));
        this.valueToKey = Map.copyOf(valueToKey);
        this.idToValue = ids.toList();
        this.tags = new ConcurrentHashMap<>(tags);
    }

    @Override
    public Key key() {
        return this.key;
    }

    @Override
    public @Nullable T get(int id) {
        return this.idToValue.get(id);
    }

    @Override
    public @Nullable T get(Key key) {
        return this.keyToValue.get(key);
    }

    @Override
    public @Nullable RegistryKey<T> getKey(int id) {
        final T value = this.idToValue.get(id);
        return value == null ? null : new RegistryKeyImpl<>(value.key());
    }

    @Override
    public @Nullable RegistryKey<T> getKey(T value) {
        return this.valueToKey.get(value);
    }

    @Override
    public @Nullable RegistryKey<T> getKey(Key key) {
        return this.keyToValue.containsKey(key) ? new RegistryKeyImpl<>(key) : null;
    }

    @Override
    public int getId(RegistryKey<T> key) {
        final T value = this.keyToValue.get(key.key());
        if (value == null) return -1; // Not found
        return this.valueToKey.get(value) != null ? value.id() : -1;
    }

    @Override
    public @Nullable DataPack getPack(int id) {
        // Static registries are always in the core data pack
        return this.idToValue.get(id) != null ? DataPack.MINECRAFT_CORE : null;
    }

    @Override
    public int size() {
        return this.keyToValue.size();
    }

    @Override
    public Collection<RegistryKey<T>> keys() {
        return this.valueToKey.values();
    }

    @Override
    public Collection<T> values() {
        return this.valueToKey.keySet();
    }

    @Override
    public @Nullable RegistryTag<T> getTag(TagKey<T> key) {
        return this.tags.get(key);
    }

    @Override
    public RegistryTag<T> getOrCreateTag(TagKey<T> key) {
        return this.tags.computeIfAbsent(key, RegistryTagImpl.Backed::new);
    }

    @Override
    public boolean removeTag(TagKey<T> key) {
        return this.tags.remove(key) != null;
    }

    @Override
    public Collection<RegistryTag<T>> tags() {
        return Collections.unmodifiableCollection(this.tags.values());
    }

    @Override
    public TagsPacket.Registry tagRegistry() {
        final List<TagsPacket.Tag> tagList = new ArrayList<>(tags.size());
        for (final RegistryTagImpl.Backed<T> tag : tags.values()) {
            final int[] entries = new int[tag.size()];
            int i = 0;
            for (var staticEntry : tag) {
                entries[i++] = staticEntry instanceof StaticProtocolObject<T> po
                        ? po.id() : getId(staticEntry);
            }
            tagList.add(new TagsPacket.Tag(tag.key().key().asString(), entries));
        }
        return new TagsPacket.Registry(key().asString(), tagList);
    }

}
