package net.minestom.server.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.ServerFlag;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.SendablePacket;
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

    record KeyImpl<T>(@NotNull net.kyori.adventure.key.Key key) implements Key<T> {

        @Override
        public String toString() {
            return key.asString();
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            KeyImpl<?> key = (KeyImpl<?>) obj;
            return this.key.equals(key.key);
        }
    }

    private Registries registries = null;
    private CachedPacket vanillaRegistryDataPacket = new CachedPacket(() -> createRegistryDataPacket(registries, true));

    private final ReentrantLock lock = new ReentrantLock(); // Protects writes
    private final List<T> entryById = new CopyOnWriteArrayList<>();
    private final Map<net.kyori.adventure.key.Key, T> entryByName = new ConcurrentHashMap<>();
    private final List<net.kyori.adventure.key.Key> idByName = new CopyOnWriteArrayList<>();
    private final List<DataPack> packById = new CopyOnWriteArrayList<>();

    private final String id;
    private final Codec<T> codec;

    DynamicRegistryImpl(@NotNull String id, @Nullable Codec<T> codec) {
        this.id = id;
        this.codec = codec;
    }

    @Override
    public @NotNull String id() {
        return id;
    }

    public @UnknownNullability Codec<T> codec() {
        return codec;
    }

    @Override
    public @Nullable T get(int id) {
        if (id < 0 || id >= entryById.size()) {
            return null;
        }
        return entryById.get(id);
    }

    @Override
    public @Nullable T get(@NotNull net.kyori.adventure.key.Key namespace) {
        return entryByName.get(namespace);
    }

    @Override
    public @Nullable Key<T> getKey(@NotNull T value) {
        int index = entryById.indexOf(value);
        return index == -1 ? null : getKey(index);
    }

    @Override
    public @Nullable Key<T> getKey(int id) {
        if (id < 0 || id >= entryById.size())
            return null;
        return Key.of(idByName.get(id));
    }

    @Override
    public @Nullable net.kyori.adventure.key.Key getName(int id) {
        if (id < 0 || id >= entryById.size())
            return null;
        return idByName.get(id);
    }

    @Override
    public @Nullable DataPack getPack(int id) {
        if (id < 0 || id >= packById.size())
            return null;
        return packById.get(id);
    }

    @Override
    public int getId(@NotNull net.kyori.adventure.key.Key id) {
        return idByName.indexOf(id);
    }

    @Override
    public @NotNull List<T> values() {
        return Collections.unmodifiableList(entryById);
    }

    @Override
    public @NotNull DynamicRegistry.Key<T> register(@NotNull net.kyori.adventure.key.Key namespaceId, @NotNull T object, @Nullable DataPack pack) {
        // This check is disabled in tests because we remake server processes over and over.
        // todo: re-enable this check
//        Check.stateCondition((!DebugUtils.INSIDE_TEST && MinecraftServer.process() != null && !MinecraftServer.isStarted()) && !ServerFlag.REGISTRY_LATE_REGISTER,
//                "Registering an object to a dynamic registry ({0}) after the server is started can lead to " +
//                        "registry desync between the client and server. This is usually unwanted behavior. If you " +
//                        "know what you're doing and would like this behavior, set the `minestom.registry.late-register` " +
//                        "system property.", id);

        lock.lock();
        try {
            int id = idByName.indexOf(namespaceId);
            entryByName.put(namespaceId, object);
            if (id == -1) {
                idByName.add(namespaceId);
                entryById.add(object);
                packById.add(pack);
            } else {
                idByName.set(id, namespaceId);
                entryById.set(id, object);
                packById.set(id, pack);
            }
            if (vanillaRegistryDataPacket != null) {
                vanillaRegistryDataPacket.invalidate();
            }
            return Key.of(namespaceId);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean remove(@NotNull net.kyori.adventure.key.Key namespaceId) throws UnsupportedOperationException {
        if (!ServerFlag.REGISTRY_UNSAFE_OPS) throw UNSAFE_REMOVE_EXCEPTION;

        lock.lock();
        try {
            int id = idByName.indexOf(namespaceId);
            if (id == -1) return false;

            entryById.remove(id);
            entryByName.remove(namespaceId);
            idByName.remove(id);
            packById.remove(id);
            if (vanillaRegistryDataPacket != null) {
                vanillaRegistryDataPacket.invalidate();
            }
            return true;
        } finally {
            lock.unlock();
        }
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

    private @NotNull RegistryDataPacket createRegistryDataPacket(@NotNull Registries registries, boolean excludeVanilla) {
        Check.notNull(codec, "Cannot create registry data packet for server-only registry");
        Transcoder<BinaryTag> transcoder = new RegistryTranscoder<>(Transcoder.NBT, registries);
        List<RegistryDataPacket.Entry> entries = new ArrayList<>(entryById.size());
        for (int i = 0; i < entryById.size(); i++) {
            CompoundBinaryTag data = null;
            // sorta todo, sorta just a note:
            // Right now we very much only support the minecraft:core (vanilla) 'pack'. Any entry which was not loaded
            // from static data will be treated as non vanilla and always sent completely. However, we really should
            // support arbitrary packs and associate all registry data with a datapack. Additionally, we should generate
            // all data for the experimental datapacks built in to vanilla such as the next update experimental (1.21 at
            // the time of writing). Datagen currently behaves kind of badly in that the registry inspecting generators
            // like material, block, etc generate entries which are behind feature flags, whereas the ones which inspect
            // static assets (the traditionally dynamic registries), do not generate those assets.
            T entry = entryById.get(i);
            DataPack pack = packById.get(i);
            if (!excludeVanilla || pack != DataPack.MINECRAFT_CORE) {
                final Result<BinaryTag> entryResult = codec.encode(transcoder, entry);
                if (entryResult instanceof Result.Ok(BinaryTag tag)) {
                    data = (CompoundBinaryTag) tag;
                } else {
                    throw new IllegalStateException("Failed to encode registry entry " + i + " (" + getKey(i) + ") for registry " + id);
                }
            }
            //noinspection DataFlowIssue
            entries.add(new RegistryDataPacket.Entry(getKey(i).name(), data));
        }
        return new RegistryDataPacket(id, entries);
    }

    static <T> void loadStaticJsonRegistry(@Nullable Registries registries, @NotNull DynamicRegistryImpl<T> registry, @NotNull RegistryData.Resource resource, @Nullable Comparator<String> idComparator) {
        Check.argCondition(!resource.fileName().endsWith(".json"), "Resource must be a JSON file: {0}", resource.fileName());
        try (InputStream resourceStream = RegistryData.loadRegistryFile(resource)) {
            Check.notNull(resourceStream, "Resource {0} does not exist!", resource);
            final JsonElement json = RegistryData.GSON.fromJson(new InputStreamReader(resourceStream, StandardCharsets.UTF_8), JsonElement.class);
            if (!(json instanceof JsonObject root))
                throw new IllegalStateException("Failed to load registry " + registry.id() + ": expected a JSON object, got " + json);

            final Transcoder<JsonElement> transcoder = registries != null ? new RegistryTranscoder<>(Transcoder.JSON, registries) : Transcoder.JSON;
            List<Map.Entry<String, JsonElement>> entries = new ArrayList<>(root.entrySet());
            if (idComparator != null) entries.sort(Map.Entry.comparingByKey(idComparator));
            for (Map.Entry<String, JsonElement> entry : entries) {
                final String namespace = entry.getKey();
                final Result<T> valueResult = registry.codec().decode(transcoder, entry.getValue());
                if (valueResult instanceof Result.Ok(T value)) {
                    registry.register(namespace, value, DataPack.MINECRAFT_CORE);
                } else {
                    throw new IllegalStateException("Failed to decode registry entry " + namespace + " for registry " + registry.id() + ": " + valueResult);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
