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
    private final Map<RegistryKey<T>, Integer> keyToId;
    private final Map<Key, T> keyToValue;
    private final Map<T, RegistryKey<T>> valueToKey;
    private final List<DataPack> packById;

    private final Map<TagKey<T>, RegistryTag<T>> tags;

    private final RegistryKey<DynamicRegistry<T>> registryKey;
    private final Codec<T> codec;

    DynamicRegistryImpl(@NotNull RegistryKey<DynamicRegistry<T>> registryKey, @Nullable Codec<T> codec) {
        this.registryKey = registryKey;
        this.codec = codec;
        // Expect stale data possibilities with unsafe ops.
        this.idToValue = new ArrayList<>();
        this.idToKey = new ArrayList<>();
        this.keyToId = new HashMap<>();
        this.keyToValue = new HashMap<>();
        this.valueToKey = new HashMap<>();
        this.packById = new ArrayList<>();
        // Tags are always mutable across the lock.
        this.tags = new ConcurrentHashMap<>();
    }

    // Used to create compressed registries
    DynamicRegistryImpl(@NotNull RegistryKey<DynamicRegistry<T>> registryKey, @Nullable Codec<T> codec, @NotNull List<T> idToValue,
                        @NotNull Map<RegistryKey<T>, Integer> keyToId, @NotNull List<RegistryKey<T>> idToKey,
                        @NotNull Map<Key, T> keyToValue, @NotNull Map<T, RegistryKey<T>> valueToKey,
                        @NotNull List<DataPack> packById, @NotNull Map<TagKey<T>, RegistryTag<T>> tags) {
        this.registryKey = registryKey;
        this.codec = codec;
        this.idToValue = idToValue;
        this.idToKey = idToKey;
        this.keyToId = keyToId;
        this.keyToValue = keyToValue;
        this.valueToKey = valueToKey;
        this.packById = packById;
        this.tags = tags;
    }

    @Override
    public @NotNull RegistryKey<DynamicRegistry<T>> registryKey() {
        return this.registryKey;
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
        return keyToId.getOrDefault(key, -1);
    }

    @Override
    public @NotNull RegistryKey<T> register(@NotNull Key key, @NotNull T object, @Nullable DataPack pack) {
        if (isFrozen()) throw new UnsupportedOperationException(UNSAFE_REMOVE_MESSAGE);

        final RegistryKey<T> registryKey = new RegistryKeyImpl<>(key);
        synchronized (REGISTRY_LOCK) {
            Integer id = keyToId.get(registryKey); // Array set at home
            keyToValue.put(key, object);
            valueToKey.put(object, registryKey);
            if (id == null) {
                idToValue.add(object);
                idToKey.add(registryKey);
                keyToId.put(registryKey, idToValue.size() - 1);
                if (pack != null) packById.add(pack); // Dont add for null, handled.
            } else {
                idToValue.set(id, object);
                idToKey.set(id, registryKey);
                keyToId.put(registryKey, id);
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
            Integer idObject = keyToId.get(registryKey);
            if (idObject == null) return false;
            int id = idObject;

            // Remove value from all mappings (shifting down indices)
            idToValue.remove(id);
            idToKey.remove(registryKey);
            keyToId.remove(registryKey);
            var value = keyToValue.remove(key);
            valueToKey.remove(value);
            packById.remove(id);

            // Remove all references from tags
            for (final var tag : tags.values()) {
                if (tag instanceof RegistryTagImpl.Backed<T> backedTag)
                    backedTag.remove(registryKey);
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
        if (!ServerFlag.REGISTRY_FREEZING_TAGS || MinecraftServer.isInitializing())
            return this.tags.computeIfAbsent(key, RegistryTagImpl.Backed::new);
        final RegistryTag<T> tag = this.tags.get(key);
        Check.notNull(tag, "Tag key `{0}` is not registered, while the tags are frozen!", key.hashedKey());
        return tag;
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
        for (final RegistryTag<T> tag : tags.values()) {
            final int[] entries = new int[tag.size()];
            int i = 0;
            for (var registryKey : tag)
                entries[i++] = keyToId.get(registryKey);
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
        final int packByIdSize = packById.size();
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
            DataPack pack = i < packByIdSize ? packById.get(i) : null;
            if (!excludeVanilla || pack != DataPack.MINECRAFT_CORE) {
                final Result<BinaryTag> entryResult = codec.encode(transcoder, entry);
                if (entryResult instanceof Result.Ok(BinaryTag tag)) {
                    data = (CompoundBinaryTag) tag;
                } else {
                    throw new IllegalStateException("Failed to encode registry entry " + i + " (" + getKey(i) + ") for registry " + registryKey.name());
                }
            }
            //noinspection DataFlowIssue
            entries.add(new RegistryDataPacket.Entry(getKey(i).key().asString(), data));
        }
        return new RegistryDataPacket(registryKey.key().asString(), entries);
    }

    /**
     * Attempts to create a copy with compressed data structures.
     *
     * @return A safe copy of this registry
     */
    @Contract(pure = true)
    @NotNull DynamicRegistryImpl<T> compact() {
        if (canFreeze()) {
            return new DynamicRegistryImpl<>(registryKey, codec,
                    List.copyOf(idToValue),
                    Map.copyOf(keyToId),
                    List.copyOf(idToKey),
                    Map.copyOf(keyToValue),
                    Map.copyOf(valueToKey),
                    List.copyOf(packById), //TODO null packById existing entry. (determine use case)
                    ServerFlag.REGISTRY_FREEZING_TAGS ? Map.copyOf(tags) : new ConcurrentHashMap<>(tags)
            );
        }
        // Create new instances so they are trimmed to size without downcasting.
        return new DynamicRegistryImpl<>(registryKey, codec,
                new ArrayList<>(idToValue),
                new HashMap<>(keyToId),
                new ArrayList<>(idToKey),
                new HashMap<>(keyToValue),
                new HashMap<>(valueToKey),
                new ArrayList<>(packById),
                new ConcurrentHashMap<>(tags)
        );
    }

    static boolean isFrozen() {
        return canFreeze() && !MinecraftServer.isInitializing();
    }

    static boolean canFreeze() {
        return !ServerFlag.REGISTRY_UNSAFE_OPS && !ServerFlag.INSIDE_TEST;
    }

    static <T> void loadStaticJsonRegistry(@Nullable Registries registries, @NotNull DynamicRegistryImpl<T> registry, @Nullable Comparator<RegistryKey<T>> idComparator, @NotNull Codec<T> codec, @Nullable DetourRegistry detourRegistry) {
        try (InputStream resourceStream = RegistryData.loadRegistryFile(String.format("%s.json", registry.key().value()))) {
            Check.notNull(resourceStream, "Resource {0} does not exist!", registry.key().value());
            final JsonElement json = JsonUtil.fromJson(new InputStreamReader(resourceStream, StandardCharsets.UTF_8));
            if (!(json instanceof JsonObject root))
                throw new IllegalStateException("Failed to load registry " + registry.key() + ": expected a JSON object, got " + json);

            // Load tags if present, Required here because the transcoder will try and read them while parsing the registry.
            Map<TagKey<T>, RegistryTag<T>> tags = RegistryData.loadTags(registry.key());
            registry.tags.putAll(tags);

            final Transcoder<JsonElement> transcoder = registries != null ? new RegistryTranscoder<>(Transcoder.JSON, registries, false) : Transcoder.JSON;
            List<Map.Entry<String, JsonElement>> entries = new ArrayList<>(root.entrySet());
            // Kind of a ugly solution, but we need to sort the entries by key to ensure that the order is deterministic.
            if (idComparator != null) entries.sort(Map.Entry.comparingByKey((a, b) -> {
                final RegistryKey<T> keyA = RegistryKey.unsafeOf(a);
                final RegistryKey<T> keyB = RegistryKey.unsafeOf(b);
                return idComparator.compare(keyA, keyB);
            }));
            for (Map.Entry<String, JsonElement> entry : entries) {
                final RegistryKey<T> key = RegistryKey.unsafeOf(entry.getKey());
                final Result<T> valueResult = codec.decode(transcoder, entry.getValue());
                if (valueResult instanceof Result.Ok(T value)) {
                    if (detourRegistry != null) value = detourRegistry.consume(key, value);
                    registry.register(key, value, DataPack.MINECRAFT_CORE);
                } else {
                    throw new IllegalStateException("Failed to decode registry entry " + key.name() + " for registry " + registry.key() + ": " + valueResult);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
