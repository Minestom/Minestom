package net.minestom.server.registry;

import net.kyori.adventure.key.Key;
import net.minestom.server.ServerFlag;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.network.packet.server.common.TagsPacket;
import net.minestom.server.utils.collection.ObjectArray;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A registry for holding static vanilla registry data. Not generally user modifiable, always immutable.
 */
@ApiStatus.Internal
final class StaticRegistry<T extends StaticProtocolObject<T>> implements Registry<T> {
    private final RegistryKey<Registry<T>> registryKey;
    private final Map<Key, T> keyToValue;
    private final Map<T, RegistryKey<T>> valueToKey;
    private final List<T> idToValue;

    private final Map<TagKey<T>, RegistryTag<T>> tags;

    StaticRegistry(
            RegistryKey<Registry<T>> registryKey,
            Map<Key, T> namespaces,
            ObjectArray<T> ids,
            Map<TagKey<T>, RegistryTag<T>> tags
    ) {
        this.registryKey = registryKey;
        this.keyToValue = Map.copyOf(namespaces);
        var valueToKey = new HashMap<T, RegistryKey<T>>(namespaces.size());
        for (var entry : namespaces.entrySet())
            valueToKey.put(entry.getValue(), new RegistryKeyImpl<>(entry.getKey()));
        this.valueToKey = Map.copyOf(valueToKey);
        this.idToValue = ids.toList();
        this.tags = ServerFlag.REGISTRY_FREEZING_TAGS ? Map.copyOf(tags) : new ConcurrentHashMap<>(tags);
    }

    @Override
    public RegistryKey<Registry<T>> registryKey() {
        return registryKey;
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
        if (!ServerFlag.REGISTRY_FREEZING_TAGS)
            return this.tags.computeIfAbsent(key, RegistryTagImpl.Backed::new);
        final RegistryTag<T> tag = this.tags.get(key);
        Check.notNull(tag, "Tag key `{0}` is not registered, while the tags are frozen!", key.hashedKey());
        return tag;
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
        for (final RegistryTag<T> entry : tags.values()) {
            if (!(entry instanceof RegistryTagImpl.Backed<T> tag)) continue;
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
