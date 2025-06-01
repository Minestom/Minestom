package net.minestom.server.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.ServerFlag;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.common.TagsPacket;
import net.minestom.server.network.packet.server.configuration.RegistryDataPacket;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

@ApiStatus.Internal
final class DynamicRegistryImpl<T> implements DynamicRegistry<T> {
    private static final UnsupportedOperationException UNSAFE_REMOVE_EXCEPTION = new UnsupportedOperationException("Unsafe remove is disabled. Enable by setting the system property 'minestom.registry.unsafe-ops' to 'true'");

    private Registries registries = null;
    private final CachedPacket vanillaRegistryDataPacket = new CachedPacket(() -> createRegistryDataPacket(registries, true));

    private final ReentrantLock lock = new ReentrantLock(); // Protects writes
    private final List<T> idToValue = new CopyOnWriteArrayList<>();
    private final List<RegistryKey<T>> idToKey = new CopyOnWriteArrayList<>();
    private final Map<Key, T> keyToValue = new ConcurrentHashMap<>();
    private final Map<T, RegistryKey<T>> valueToKey = new ConcurrentHashMap<>();
    private final List<DataPack> packById = new CopyOnWriteArrayList<>();

    private final Map<TagKey<T>, RegistryTagImpl.Backed<T>> tags = new ConcurrentHashMap<>();

    private final Key key;
    private final Codec<T> codec;

    DynamicRegistryImpl(@NotNull Key key, @Nullable Codec<T> codec) {
        this.key = key;
        this.codec = codec;
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
        // This check is disabled in tests because we remake server processes over and over.
        // todo: re-enable this check
//        Check.stateCondition((!DebugUtils.INSIDE_TEST && MinecraftServer.process() != null && !MinecraftServer.isStarted()) && !ServerFlag.REGISTRY_LATE_REGISTER,
//                "Registering an object to a dynamic registry ({0}) after the server is started can lead to " +
//                        "registry desync between the client and server. This is usually unwanted behavior. If you " +
//                        "know what you're doing and would like this behavior, set the `minestom.registry.late-register` " +
//                        "system property.", id);

        lock.lock();
        try {
            final RegistryKey<T> registryKey = new RegistryKeyImpl<>(key);
            int id = idToKey.indexOf(registryKey);
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
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean remove(@NotNull Key key) throws UnsupportedOperationException {
        if (!ServerFlag.REGISTRY_UNSAFE_OPS) throw UNSAFE_REMOVE_EXCEPTION;

        lock.lock();
        try {
            final RegistryKey<T> registryKey = new RegistryKeyImpl<>(key);
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
        } finally {
            lock.unlock();
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

    @Override
    public @NotNull SendablePacket registryDataPacket(@NotNull Registries registries, boolean excludeVanilla) {
        // We cache the vanilla packet because that is by far the most common case. If some client claims not to have
        // the vanilla datapack we can compute the entire thing.
        if (excludeVanilla) {
            if (this.registries != registries) {
                vanillaRegistryDataPacket.invalidate();
                this.registries = registries;
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

    static <T> void loadStaticJsonRegistry(@Nullable Registries registries, @NotNull DynamicRegistryImpl<T> registry, @NotNull RegistryData.Resource resource, @Nullable Comparator<String> idComparator) {
        Check.argCondition(!resource.fileName().endsWith(".json"), "Resource must be a JSON file: {0}", resource.fileName());
        try (InputStream resourceStream = RegistryData.loadRegistryFile(String.format("%s.json", registry.key().value()))) {
            Check.notNull(resourceStream, "Resource {0} does not exist!", resource);
            final JsonElement json = RegistryData.GSON.fromJson(new InputStreamReader(resourceStream, StandardCharsets.UTF_8), JsonElement.class);
            if (!(json instanceof JsonObject root))
                throw new IllegalStateException("Failed to load registry " + registry.key() + ": expected a JSON object, got " + json);

            final Transcoder<JsonElement> transcoder = registries != null ? new RegistryTranscoder<>(Transcoder.JSON, registries, false, true) : Transcoder.JSON;
            List<Map.Entry<String, JsonElement>> entries = new ArrayList<>(root.entrySet());
            if (idComparator != null) entries.sort(Map.Entry.comparingByKey(idComparator));
            for (Map.Entry<String, JsonElement> entry : entries) {
                final String namespace = entry.getKey();
                final Result<T> valueResult = registry.codec().decode(transcoder, entry.getValue());
                if (valueResult instanceof Result.Ok(T value)) {
                    registry.register(namespace, value, DataPack.MINECRAFT_CORE);
                } else {
                    throw new IllegalStateException("Failed to decode registry entry " + namespace + " for registry " + registry.key() + ": " + valueResult);
                }
            }

            // Load tags if present
            RegistryData.loadTags(registry, registry.key());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
