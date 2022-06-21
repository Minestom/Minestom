package net.minestom.server.registry.dynamic;

import net.kyori.adventure.key.Key;
import net.minestom.server.Configuration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.registry.NBTRepresentable;
import net.minestom.server.registry.dynamic.chat.ChatType;
import net.minestom.server.registry.dynamic.chat.DynamicChatTypeImpl;
import net.minestom.server.utils.ObjectCache;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTType;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public final class DynamicRegistryManager implements NBTRepresentable {
    private final Set<Key> registries = new HashSet<>();
    private final Map<Key, AtomicInteger> idCounters = new HashMap<>();
    private final Map<Key, ObjectCache<NBTCompound>> compoundCaches = new HashMap<>();
    private final Map<Key, List<NBTCompound>> registryEntries = new HashMap<>();
    private final Map<Key, Map<Key, NBTCompound>> registryEntriesByName = new HashMap<>();

    @Contract("_, _, null -> null")
    public <T extends DynamicRegistryEntry> T register(@NotNull Key registry, @NotNull NBTCompound data,
                                                       @Nullable Function<NBTCompound, T> factory) {
        final @Subst("minecraft:something") String name = data.getString("name");
        if (name == null) {
            throw new IllegalArgumentException("Data doesn't have required name tag!");
        }

        final int id = idCounters.computeIfAbsent(registry, k -> new AtomicInteger()).getAndIncrement();
        final NBTCompound compound = new MutableNBTCompound(data).setInt("id", id).toCompound();

        storeEntry(registry, Key.key(name), compound);

        return factory == null ? null : factory.apply(compound);
    }

    public <T extends DynamicRegistryEntry> T register(DynamicRegistryEntryBuilder<T> builder) {
        final T entry = builder.build(idCounters.computeIfAbsent(builder.registry(), k -> new AtomicInteger()).getAndIncrement());
        final NBTCompound compound = entry.toNBT();
        storeEntry(entry.registry(), entry.key(), compound);
        return entry;
    }

    private void storeEntry(Key registry, Key name, NBTCompound entry) {
        registries.add(registry);
        registryEntries.computeIfAbsent(registry, k -> new ArrayList<>()).add(entry);
        registryEntriesByName.computeIfAbsent(registry, k -> new HashMap<>()).put(name, entry);

        compoundCaches.computeIfAbsent(registry, k -> new ObjectCache<>(() -> {
            final MutableNBTCompound root = new MutableNBTCompound();
            root.set("type", NBT.String(k.asString()));
            root.set("value", NBT.List(NBTType.TAG_Compound, registryEntries.get(k)));
            return root.toCompound();
        })).invalidate();
    }

    public NBTCompound toNBT(Key registry) {
        return compoundCaches.get(registry).get();
    }

    @Override
    public NBTCompound toNBT() {
        final MutableNBTCompound result = new MutableNBTCompound();
        for (Key registry : registries) {
            result.set(registry.toString(), toNBT(registry));
        }
        return result.toCompound();
    }

    @ApiStatus.Internal
    public void initDefaults() {
        final Configuration config = MinecraftServer.getConfiguration();
        ((DynamicChatTypeImpl) ChatType.CHAT).setBackingType(register(config.playerChatType()));
        ((DynamicChatTypeImpl) ChatType.SYSTEM).setBackingType(register(config.systemChatType()));
    }
}
