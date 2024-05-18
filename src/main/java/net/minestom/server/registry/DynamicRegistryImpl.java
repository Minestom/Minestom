package net.minestom.server.registry;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.ServerFlag;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.configuration.RegistryDataPacket;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

@ApiStatus.Internal
public class DynamicRegistryImpl<T extends ProtocolObject> implements DynamicRegistry<T> {
    private static final UnsupportedOperationException UNSAFE_REMOVE_EXCEPTION = new UnsupportedOperationException("Unsafe remove is disabled. Enable by setting the system property 'minestom.registry.unsafe-remove' to 'true'");

    private final CachedPacket vanillaRegistryDataPacket = new CachedPacket(() -> createRegistryDataPacket(true));

    private final ReentrantLock lock = new ReentrantLock(); // Protects entryById (except when used alone, it is safe), entryByName, and idByName
    private final List<T> entryById = new CopyOnWriteArrayList<>(); // We use a CopyOnWriteArrayList even with the lock above because it handles concurrent iteration
    private final Map<NamespaceID, T> entryByName = new HashMap<>();
    private final List<NamespaceID> idByName = new ArrayList<>();

    private final String id;
    private final BinaryTagSerializer<T> nbtType;

    public DynamicRegistryImpl(@NotNull String id, BinaryTagSerializer<T> nbtType) {
        this.id = id;
        this.nbtType = nbtType;
    }

    public DynamicRegistryImpl(@NotNull String id, BinaryTagSerializer<T> nbtType, @NotNull Registry.Resource resource, @NotNull Registry.Container.Loader<T> loader) {
        this(id, nbtType);
        loadStaticRegistry(resource, loader);
    }

    @Override
    public @Nullable T get(int id) {
        if (id < 0 || id >= entryById.size()) {
            return null;
        }
        return entryById.get(id);
    }

    @Override
    public @Nullable T get(@NotNull NamespaceID namespace) {
        lock.lock();
        try {
            return entryByName.get(namespace);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int getId(@NotNull NamespaceID id) {
        lock.lock();
        try {
            return idByName.indexOf(id);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public @NotNull List<T> values() {
        return Collections.unmodifiableList(entryById);
    }

    @Override
    public int register(@NotNull T object) {
        lock.lock();
        try {
            int id = idByName.indexOf(object.namespace());
            if (id == -1) id = entryById.size();

            entryById.add(id, object);
            entryByName.put(object.namespace(), object);
            idByName.add(object.namespace());
            vanillaRegistryDataPacket.invalidate();
            return id;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean remove(@NotNull T object) throws UnsupportedOperationException {
        if (!ServerFlag.REGISTRY_UNSAFE_REMOVE) throw UNSAFE_REMOVE_EXCEPTION;

        lock.lock();
        try {
            int id = idByName.indexOf(object.namespace());
            if (id == -1) return false;

            entryById.remove(id);
            entryByName.remove(object.namespace());
            idByName.remove(id);
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
        lock.lock();
        try {
            var entries = new ArrayList<RegistryDataPacket.Entry>(entryById.size());
            for (var entry : entryById) {
                CompoundBinaryTag data = null;
                // sorta todo, sorta just a note:
                // Right now we very much only support the minecraft:core (vanilla) 'pack'. Any entry which was not loaded
                // from static data will be treated as non vanilla and always sent completely. However, we really should
                // support arbitrary packs and associate all registry data with a datapack. Additionally, we should generate
                // all data for the experimental datapacks built in to vanilla such as the next update experimental (1.21 at
                // the time of writing). Datagen currently behaves kind of badly in that the registry inspecting generators
                // like material, block, etc generate entries which are behind feature flags, whereas the ones which inspect
                // static assets (the traditionally dynamic registries), do not generate those assets.
                if (!excludeVanilla || entry.registry() == null) {
                    data = (CompoundBinaryTag) nbtType.write(entry);
                }
                entries.add(new RegistryDataPacket.Entry(entry.name(), data));
            }
            return new RegistryDataPacket(id, entries);
        } finally {
            lock.unlock();
        }
    }

    private void loadStaticRegistry(@NotNull Registry.Resource resource, @NotNull Registry.Container.Loader<T> loader) {
        for (var entry : Registry.load(resource).entrySet()) {
            final String namespace = entry.getKey();
            final Registry.Properties properties = Registry.Properties.fromMap(entry.getValue());
            register(loader.get(namespace, properties));
        }
    }
}
