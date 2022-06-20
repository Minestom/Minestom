package net.minestom.server.registry.dynamic;

import net.kyori.adventure.key.Key;
import net.minestom.server.ConfigurationManager;
import net.minestom.server.MinecraftServer;
import net.minestom.server.registry.NBTRepresentable;
import net.minestom.server.registry.dynamic.chat.ChatType;
import net.minestom.server.registry.dynamic.chat.DynamicChatTypeImpl;
import net.minestom.server.utils.ObjectCache;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.ApiStatus;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTType;
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class DynamicRegistryManager implements NBTRepresentable {
    private final Set<Key> registries = new HashSet<>();
    private final Map<Key, AtomicInteger> idCounters = new HashMap<>();
    private final Map<Key, ObjectCache<NBTCompound>> compoundCaches = new HashMap<>();
    private final Map<Key, List<NBTCompound>> registryElements = new HashMap<>();
    private final Map<Key, Map<Key, NBTCompound>> registryElementsByName = new HashMap<>();

    public <T extends DynamicRegistryElement> T register(Key registry, DynamicRegistryElementFactory<T> factory, NBTCompound data) {
        final @Subst("minecraft:something") String name = data.getString("name");
        if (name == null) {
            throw new IllegalArgumentException("Data doesn't have required name tag!");
        }

        registries.add(registry);

        final int id = idCounters.computeIfAbsent(registry, k -> new AtomicInteger()).getAndIncrement();

        final MutableNBTCompound compound = new MutableNBTCompound(data);
        compound.setInt("id", id);
        final NBTCompound dataWithId = compound.toCompound();
        final T result = factory.apply(dataWithId);
        registryElements.computeIfAbsent(registry, k -> new ArrayList<>()).add(dataWithId);
        registryElementsByName.computeIfAbsent(Key.key(name), k -> new HashMap<>()).put(result.key(), dataWithId);

        compoundCaches.computeIfAbsent(registry, k -> new ObjectCache<>(() -> {
            final MutableNBTCompound root = new MutableNBTCompound();
            root.set("type", NBT.String(k.asString()));
            root.set("value", NBT.List(NBTType.TAG_Compound, registryElements.get(k)));
            return root.toCompound();
        })).invalidate();

        return result;
    }

    public <T extends DynamicRegistryElement> T register(DynamicRegistryElementBuilder<T> builder) {
        return register(builder.registry(), builder.factory(), builder.toNBT());
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
        final ConfigurationManager config = MinecraftServer.getConfigurationManager();
        ((DynamicChatTypeImpl) ChatType.CHAT).setBackingType(register(config.PLAYER_CHAT_TYPE.get()));
        ((DynamicChatTypeImpl) ChatType.SYSTEM).setBackingType(register(config.SYSTEM_CHAT_TYPE.get()));
    }
}
