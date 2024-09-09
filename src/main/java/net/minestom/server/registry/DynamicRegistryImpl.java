package net.minestom.server.registry;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.gamedata.DataPack;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.configuration.RegistryDataPacket;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

final class DynamicRegistryImpl<T extends ProtocolObject> implements DynamicRegistry<T> {
    private static final UnsupportedOperationException UNSAFE_REMOVE_EXCEPTION = new UnsupportedOperationException("Unsafe remove is disabled. Enable by setting the system property 'minestom.registry.unsafe-ops' to 'true'");

    record KeyImpl<T>(@NotNull NamespaceID namespace) implements Key<T> {
        @Override
        public String toString() {
            return namespace.asString();
        }
    }

    private final CachedPacket vanillaRegistryDataPacket = new CachedPacket(() -> createRegistryDataPacket(true));

    private final ReentrantLock lock = new ReentrantLock(); // Protects writes

    /**
     * The index of the registry. This is a volatile field because it is only ever replaced, never mutated. This means
     * that reads are always consistent and up-to-date, and writes are atomic and do not require synchronization.
     * <p>
     * Be careful to only read this field once, and store the result in a local variable.
     */
    private volatile Index<T> index = new Index<>(List.of(), Map.of(), List.of(), List.of());

    private final String id;
    private final BinaryTagSerializer<T> nbtType;

    record Index<T>(
            List<T> idToEntry,
            Map<NamespaceID, T> nameToEntry,
            List<NamespaceID> idToName,
            List<DataPack> idToPack
    ) {
        public Index {
            idToEntry = List.copyOf(idToEntry);
            nameToEntry = Map.copyOf(nameToEntry);
            idToName = List.copyOf(idToName);
            idToPack = new ArrayList<>(idToPack); // Can contain nulls
        }
    }

    DynamicRegistryImpl(@NotNull String id, @Nullable BinaryTagSerializer<T> nbtType) {
        this.id = id;
        this.nbtType = nbtType;
    }

    @Override
    public @NotNull String id() {
        return id;
    }

    public @UnknownNullability BinaryTagSerializer<T> nbtType() {
        return nbtType;
    }

    @Override
    public @Nullable T get(int id) {
        final List<T> entryById = index.idToEntry;
        if (id < 0 || id >= entryById.size()) {
            return null;
        }
        return entryById.get(id);
    }

    @Override
    public @Nullable T get(@NotNull NamespaceID namespace) {
        return index.nameToEntry.get(namespace);
    }

    @Override
    public @Nullable Key<T> getKey(@NotNull T value) {
        final Index<T> index = this.index;
        final List<T> entryById = index.idToEntry;
        final int id = entryById.indexOf(value);
        if (id == -1 || id >= entryById.size()) return null;
        return Key.of(index.idToName.get(id));
    }

    @Override
    public @Nullable Key<T> getKey(int id) {
        final Index<T> index = this.index;
        if (id < 0 || id >= index.idToEntry.size()) return null;
        return Key.of(index.idToName.get(id));
    }

    @Override
    public @Nullable NamespaceID getName(int id) {
        final Index<T> index = this.index;
        if (id < 0 || id >= index.idToEntry.size()) return null;
        return index.idToName.get(id);
    }

    @Override
    public @Nullable DataPack getPack(int id) {
        final List<DataPack> packById = index.idToPack;
        if (id < 0 || id >= packById.size())
            return null;
        return packById.get(id);
    }

    @Override
    public int getId(@NotNull NamespaceID id) {
        return index.idToName.indexOf(id);
    }

    @Override
    public @NotNull List<T> values() {
        return index.idToEntry;
    }

    @Override
    public @NotNull DynamicRegistry.Key<T> register(@NotNull T object, @Nullable DataPack pack) {
        // This check is disabled in tests because we remake server processes over and over.
        // todo: re-enable this check
//        Check.stateCondition((!DebugUtils.INSIDE_TEST && MinecraftServer.process() != null && !MinecraftServer.isStarted()) && !ServerFlag.REGISTRY_LATE_REGISTER,
//                "Registering an object to a dynamic registry ({0}) after the server is started can lead to " +
//                        "registry desync between the client and server. This is usually unwanted behavior. If you " +
//                        "know what you're doing and would like this behavior, set the `minestom.registry.late-register` " +
//                        "system property.", id);


        final NamespaceID namespace = object.namespace();
        lock.lock();
        try {
            final Index<T> oldIndex = index;
            int id = oldIndex.idToName.indexOf(namespace);
            if (id == -1) id = oldIndex.nameToEntry.size();

            List<T> idToEntry = new ArrayList<>(oldIndex.idToEntry);
            Map<NamespaceID, T> nameToEntry = new HashMap<>(oldIndex.nameToEntry);
            List<NamespaceID> idToName = new ArrayList<>(oldIndex.idToName);
            List<DataPack> idToPack = new ArrayList<>(oldIndex.idToPack);

            idToEntry.add(id, object);
            nameToEntry.put(namespace, object);
            idToName.add(namespace);
            idToPack.add(id, pack);

            this.index = new Index<>(idToEntry, nameToEntry, idToName, idToPack);

            vanillaRegistryDataPacket.invalidate();
            return Key.of(namespace);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean remove(@NotNull NamespaceID namespaceId) throws UnsupportedOperationException {
        if (!ServerFlag.REGISTRY_UNSAFE_OPS) throw UNSAFE_REMOVE_EXCEPTION;

        lock.lock();
        try {
            final Index<T> oldIndex = index;
            final int id = oldIndex.idToName.indexOf(namespaceId);
            if (id == -1) return false;

            List<T> idToEntry = new ArrayList<>(oldIndex.idToEntry);
            Map<NamespaceID, T> nameToEntry = new HashMap<>(oldIndex.nameToEntry);
            List<NamespaceID> idToName = new ArrayList<>(oldIndex.idToName);
            List<DataPack> idToPack = new ArrayList<>(oldIndex.idToPack);

            idToEntry.remove(id);
            nameToEntry.remove(namespaceId);
            idToName.remove(id);
            idToPack.remove(id);

            this.index = new Index<>(idToEntry, nameToEntry, idToName, idToPack);

            vanillaRegistryDataPacket.invalidate();
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public @NotNull SendablePacket registryDataPacket(boolean excludeVanilla) {
        // We cache the vanilla packet because that is by far the most common case. If some client claims not to have
        // the vanilla datapack we can compute the entire thing.
        return excludeVanilla ? vanillaRegistryDataPacket : createRegistryDataPacket(false);
    }

    private @NotNull RegistryDataPacket createRegistryDataPacket(boolean excludeVanilla) {
        Check.notNull(nbtType, "Cannot create registry data packet for server-only registry");
        BinaryTagSerializer.Context context = new BinaryTagSerializer.ContextWithRegistries(MinecraftServer.process(), true);
        final Index<T> index = this.index;
        final List<T> entryById = index.idToEntry;
        final List<DataPack> packById = index.idToPack;
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
                data = (CompoundBinaryTag) nbtType.write(context, entry);
            }
            //noinspection DataFlowIssue
            entries.add(new RegistryDataPacket.Entry(getKey(i).name(), data));
        }
        return new RegistryDataPacket(id, entries);
    }

    static <T extends ProtocolObject> void loadStaticSnbtRegistry(@NotNull Registries registries, @NotNull DynamicRegistryImpl<T> registry, @NotNull Registry.Resource resource) {
        final Map<String, CompoundBinaryTag> map = Registry.loadSnbt(resource);
        final BinaryTagSerializer.Context context = new BinaryTagSerializer.ContextWithRegistries(registries, false);
        for (var entry : map.entrySet()) {
            final String namespace = entry.getKey();
            final CompoundBinaryTag tag = entry.getValue();
            // TODO must forward namespace
            final T value = registry.nbtType.read(context, tag);
            registry.register(value, DataPack.MINECRAFT_CORE);
        }
    }
}
