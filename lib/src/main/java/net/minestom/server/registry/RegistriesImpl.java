package net.minestom.server.registry;

import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.common.TagsPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

final class RegistriesImpl {
    // Settable default-Registries holder. The framework installs the live ServerProcess on startup;
    // lib code that needs registries but has no explicit source reads this. Defaults lazily to vanilla().
    private static final AtomicReference<Registries> STATIC_REGISTRIES = new AtomicReference<>();

    private RegistriesImpl() {
    }

    static Registries staticRegistries() {
        Registries registries = STATIC_REGISTRIES.get();
        if (registries == null) {
            registries = Registries.vanilla();
            if (!STATIC_REGISTRIES.compareAndSet(null, registries)) {
                registries = STATIC_REGISTRIES.get();
            }
        }
        return registries;
    }

    static void staticRegistries(Registries registries) {
        STATIC_REGISTRIES.set(registries);
    }

    static List<SendablePacket> registryDataPackets(Registries registries, boolean excludeVanilla) {
        final List<SendablePacket> packets = new ArrayList<>();
        for (DynamicRegistry<?> registry : configurationRegistries(registries)) {
            packets.add(registry.registryDataPacket(registries, excludeVanilla));
        }
        return packets;
    }

    static TagsPacket tagsPacket(Registries registries) {
        final List<TagsPacket.Registry> entries = new ArrayList<>();
        for (Registry<?> registry : tagRegistries(registries)) {
            entries.add(registry.tagRegistry());
        }
        return new TagsPacket(entries);
    }

    private static List<DynamicRegistry<?>> configurationRegistries(Registries registries) {
        return List.of(
                registries.chatType(),
                registries.biome(),
                registries.dialog(),
                registries.damageType(),
                registries.trimMaterial(),
                registries.trimPattern(),
                registries.bannerPattern(),
                registries.enchantment(),
                registries.paintingVariant(),
                registries.jukeboxSong(),
                registries.instrument(),
                registries.wolfVariant(),
                registries.wolfSoundVariant(),
                registries.catVariant(),
                registries.catSoundVariant(),
                registries.chickenVariant(),
                registries.chickenSoundVariant(),
                registries.cowVariant(),
                registries.cowSoundVariant(),
                registries.frogVariant(),
                registries.pigVariant(),
                registries.pigSoundVariant(),
                registries.zombieNautilusVariant(),
                registries.worldClock(),
                registries.timeline(),
                registries.dimensionType(),
                registries.sulfurCubeArchetype()
        );
    }

    private static List<Registry<?>> tagRegistries(Registries registries) {
        // These are the registries which contain tags used by the vanilla client.
        // Registries unused by the client do not need to be included.
        final List<Registry<?>> entries = new ArrayList<>();
        entries.add(registries.blocks());
        entries.add(registries.entityType());
        entries.add(registries.fluid());
        entries.add(registries.gameEvent());
        entries.add(registries.material());
        entries.addAll(configurationRegistries(registries));
        return entries;
    }
}
