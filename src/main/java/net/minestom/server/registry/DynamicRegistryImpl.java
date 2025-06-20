package net.minestom.server.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.common.TagsPacket;
import net.minestom.server.network.packet.server.configuration.RegistryDataPacket;
import net.minestom.server.utils.json.JsonUtil;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ApiStatus.Internal
final class DynamicRegistryImpl<T> implements DynamicRegistry<T> {
    private static final String UNSAFE_REMOVE_MESSAGE = "Unsafe remove is disabled. Enable by setting the system property 'minestom.registry.unsafe-ops' to 'true'";
    // Could also just use `this`, but this is a good candidate for identityless classes.
    // Also, what use case requires you to mutate registries faster than one monitor?
    private static final Object REGISTRY_LOCK = new Object();

    private volatile Registries registries = null;
    private final CachedPacket vanillaRegistryDataPacket = new CachedPacket(() -> createRegistryDataPacket(registries, true));

    private final List<T> idToValue;
    private final List<RegistryKey<T>> idToKey;
    private final Map<Key, T> keyToValue;
    private final Map<T, RegistryKey<T>> valueToKey;
    private final List<DataPack> packById;

    private final Map<TagKey<T>, RegistryTagImpl.Backed<T>> tags;

    private final Key key;
    private final Codec<T> codec;

    DynamicRegistryImpl(@NotNull Key key, @Nullable Codec<T> codec) {
        this.key = key;
        this.codec = codec;
        // Expect stale data possibilities with unsafe ops.
        this.idToValue = new ArrayList<>();
        this.idToKey = new ArrayList<>();
        this.keyToValue = new HashMap<>();
        this.valueToKey = new HashMap<>();
        this.packById = new ArrayList<>();
        // Tags are always mutable across the lock.
        this.tags = new ConcurrentHashMap<>();
    }

    // Used to create compressed registries
    DynamicRegistryImpl(@NotNull Key key, @Nullable Codec<T> codec, @NotNull List<T> idToValue,
                        @NotNull List<RegistryKey<T>> idToKey, @NotNull Map<Key, T> keyToValue,
                        @NotNull Map<T, RegistryKey<T>> valueToKey, @NotNull List<DataPack> packById,
                        @NotNull Map<TagKey<T>, RegistryTagImpl.Backed<T>> tags) {
        this.key = key;
        this.codec = codec;
        this.idToValue = idToValue;
        this.idToKey = idToKey;
        this.keyToValue = keyToValue;
        this.valueToKey = valueToKey;
        this.packById = packById;
        this.tags = tags;
    }

    @Override
    public @NotNull Key key() {
        return this.key;
    }

    public @UnknownNullability Codec<T> codec() {
        return codec;
    }

    @Override
    public @Nullable T get(int id) {
        if (id < 0 || id >= idToValue.size())
            return null;
        return idToValue.get(id);
    }

    @Override
    public @Nullable T get(@NotNull Key key) {
        return keyToValue.get(key);
    }

    @Override
    public @Nullable RegistryKey<T> getKey(int id) {
        if (id < 0 || id >= idToKey.size())
            return null;
        return idToKey.get(id);
    }

    @Override
    public @Nullable RegistryKey<T> getKey(@NotNull T value) {
        return valueToKey.get(value);
    }

    @Override
    public @Nullable RegistryKey<T> getKey(@NotNull Key key) {
        if (!keyToValue.containsKey(key))
            return null;
        return new RegistryKeyImpl<>(key);
    }

    @Override
    public int getId(@NotNull RegistryKey<T> key) {
        return idToKey.indexOf(key);
    }

    @Override
    public @NotNull RegistryKey<T> register(@NotNull Key key, @NotNull T object, @Nullable DataPack pack) {
        if (isFrozen()) throw new UnsupportedOperationException(UNSAFE_REMOVE_MESSAGE);

        final RegistryKey<T> registryKey = new RegistryKeyImpl<>(key);
        synchronized (REGISTRY_LOCK) {
            int id = idToKey.indexOf(registryKey); // Array set at home
            keyToValue.put(key, object);
            valueToKey.put(object, registryKey);
            if (id == -1) {
                idToValue.add(object);
                idToKey.add(registryKey);
                packById.add(pack);
            } else {
                idToValue.set(id, object);
                idToKey.set(id, registryKey);
                packById.set(id, pack);
            }

            vanillaRegistryDataPacket.invalidate();
            return registryKey;
        }
    }

    @Override
    public boolean remove(@NotNull Key key) throws UnsupportedOperationException {
        if (isFrozen()) throw new UnsupportedOperationException(UNSAFE_REMOVE_MESSAGE);

        final RegistryKey<T> registryKey = new RegistryKeyImpl<>(key);
        synchronized (REGISTRY_LOCK) {
            int id = idToKey.indexOf(registryKey);
            if (id == -1) return false;

            // Remove value from all mappings (shifting down indices)
            idToValue.remove(id);
            idToKey.remove(registryKey);
            var value = keyToValue.remove(key);
            valueToKey.remove(value);
            packById.remove(id);

            // Remove all references from tags
            for (final var tag : tags.values()) {
                tag.remove(registryKey);
            }

            vanillaRegistryDataPacket.invalidate();
            return true;
        }
    }

    @Override
    public @Nullable DataPack getPack(int id) {
        if (id < 0 || id >= packById.size())
            return null;
        return packById.get(id);
    }

    @Override
    public int size() {
        return idToValue.size();
    }

    @Override
    public @NotNull Collection<RegistryKey<T>> keys() {
        return Collections.unmodifiableCollection(idToKey);
    }

    @Override
    public @NotNull Collection<T> values() {
        return Collections.unmodifiableCollection(idToValue);
    }

    // Tags

    @Override
    public @Nullable RegistryTag<T> getTag(@NotNull TagKey<T> key) {
        return this.tags.get(key);
    }

    @Override
    public @NotNull RegistryTag<T> getOrCreateTag(@NotNull TagKey<T> key) {
        return this.tags.computeIfAbsent(key, RegistryTagImpl.Backed::new);
    }

    @Override
    public boolean removeTag(@NotNull TagKey<T> key) {
        return this.tags.remove(key) != null;
    }

    @Override
    public @NotNull Collection<RegistryTag<T>> tags() {
        return Collections.unmodifiableCollection(this.tags.values());
    }

    @Override // This method is called by a virtual thread in the configuration phase
    public @NotNull SendablePacket registryDataPacket(@NotNull Registries registries, boolean excludeVanilla) {
        // We cache the vanilla packet because that is by far the most common case. If some client claims not to have
        // the vanilla datapack we can compute the entire thing.
        if (excludeVanilla) {
            if (this.registries != registries) {
                synchronized (REGISTRY_LOCK) { // Bootleg off the static lock for this mutation
                    if (this.registries != registries) {
                        this.registries = registries;
                        vanillaRegistryDataPacket.invalidate();
                    }
                }
            }
            return vanillaRegistryDataPacket;
        }

        return createRegistryDataPacket(registries, false);
    }

    @Override
    public TagsPacket.@NotNull Registry tagRegistry() {
        final List<TagsPacket.Tag> tagList = new ArrayList<>(tags.size());
        for (final RegistryTagImpl.Backed<T> tag : tags.values()) {
            final int[] entries = new int[tag.size()];
            int i = 0;
            for (var registryKey : tag)
                entries[i++] = idToKey.indexOf(registryKey);
            tagList.add(new TagsPacket.Tag(tag.key().key().asString(), entries));
        }
        return new TagsPacket.Registry(key().asString(), tagList);
    }

    private @NotNull RegistryDataPacket createRegistryDataPacket(@NotNull Registries registries, boolean excludeVanilla) {
        Check.notNull(codec, "Cannot create registry data packet for server-only registry");
        Transcoder<BinaryTag> transcoder = new RegistryTranscoder<>(Transcoder.NBT, registries);
        // Copy to avoid concurrent modification issues while iterating, as we are not synchronized on the registry
        final List<T> idToValue;
        final List<DataPack> packById;
        if (!canFreeze()) {
            synchronized (REGISTRY_LOCK) {
                idToValue = List.copyOf(this.idToValue);
                packById = List.copyOf(this.packById);
            }
        } else {
            idToValue = this.idToValue;
            packById = this.packById;
        }
        List<RegistryDataPacket.Entry> entries = new ArrayList<>(idToValue.size());
        for (int i = 0; i < idToValue.size(); i++) {
            CompoundBinaryTag data = null;
            // sorta todo, sorta just a note:
            // Right now we very much only support the minecraft:core (vanilla) 'pack'. Any entry which was not loaded
            // from static data will be treated as non vanilla and always sent completely. However, we really should
            // support arbitrary packs and associate all registry data with a datapack. Additionally, we should generate
            // all data for the experimental datapacks built in to vanilla such as the next update experimental (1.21 at
            // the time of writing). Datagen currently behaves kind of badly in that the registry inspecting generators
            // like material, block, etc generate entries which are behind feature flags, whereas the ones which inspect
            // static assets (the traditionally dynamic registries), do not generate those assets.
            T entry = idToValue.get(i);
            DataPack pack = packById.get(i);
            if (!excludeVanilla || pack != DataPack.MINECRAFT_CORE) {
                final Result<BinaryTag> entryResult = codec.encode(transcoder, entry);
                if (entryResult instanceof Result.Ok(BinaryTag tag)) {
                    data = (CompoundBinaryTag) tag;
                } else {
                    throw new IllegalStateException("Failed to encode registry entry " + i + " (" + getKey(i) + ") for registry " + key);
                }
            }
            //noinspection DataFlowIssue
            entries.add(new RegistryDataPacket.Entry(getKey(i).key().asString(), data));
        }
        return new RegistryDataPacket(key.asString(), entries);
    }

    /**
     * Attempts to create a copy with compressed data structures.
     *
     * @return A safe copy of this registry
     */
    @Contract(pure = true)
    @NotNull DynamicRegistryImpl<T> compact() {
        // Create new instances so they are trimmed to size without downcasting.
        return new DynamicRegistryImpl<>(key, codec,
                new ArrayList<>(idToValue),
                new ArrayList<>(idToKey),
                new HashMap<>(keyToValue),
                new HashMap<>(valueToKey),
                new ArrayList<>(packById),
                new ConcurrentHashMap<>(tags)
        );
    }

    static boolean isFrozen() {
        return canFreeze() && MinecraftServer.process() != null && MinecraftServer.isStarted();
    }

    static boolean canFreeze() {
        return !ServerFlag.REGISTRY_UNSAFE_OPS && !ServerFlag.INSIDE_TEST;
    }

    static <T> void loadStaticJsonRegistry(@Nullable Registries registries, @NotNull DynamicRegistryImpl<T> registry, @NotNull RegistryData.Resource resource, @Nullable Comparator<String> idComparator, @NotNull Codec<T> codec) {
        Check.argCondition(!resource.fileName().endsWith(".json"), "Resource must be a JSON file: {0}", resource.fileName());
        try (InputStream resourceStream = RegistryData.loadRegistryFile(String.format("%s.json", registry.key().value()))) {
            Check.notNull(resourceStream, "Resource {0} does not exist!", resource);
            final JsonElement json = JsonUtil.fromJson(new InputStreamReader(resourceStream, StandardCharsets.UTF_8));
            if (!(json instanceof JsonObject root))
                throw new IllegalStateException("Failed to load registry " + registry.key() + ": expected a JSON object, got " + json);

            final Transcoder<JsonElement> transcoder = registries != null ? new RegistryTranscoder<>(Transcoder.JSON, registries, false, true) : Transcoder.JSON;
            List<Map.Entry<String, JsonElement>> entries = new ArrayList<>(root.entrySet());
            if (idComparator != null) entries.sort(Map.Entry.comparingByKey(idComparator));
            for (Map.Entry<String, JsonElement> entry : entries) {
                final String namespace = entry.getKey();
                final Result<T> valueResult = codec.decode(transcoder, entry.getValue());
                if (valueResult instanceof Result.Ok(T value)) {
                    registry.register(namespace, value, DataPack.MINECRAFT_CORE);
                } else {
                    throw new IllegalStateException("Failed to decode registry entry " + namespace + " for registry " + registry.key() + ": " + valueResult);
                }
            }

            // Load tags if present
            Map<TagKey<T>, RegistryTagImpl.Backed<T>> tags = RegistryData.loadTags(registry.key());
            registry.tags.putAll(tags);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
