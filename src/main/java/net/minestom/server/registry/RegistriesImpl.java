package net.minestom.server.registry;

import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.common.TagsPacket;
import net.minestom.server.network.packet.server.configuration.RegistryDataPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class RegistriesImpl {
    private RegistriesImpl() {
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

    static void applyRegistryDataPacket(Registries registries, RegistryDataPacket packet) {
        Objects.requireNonNull(packet, "Packet cannot be null");
        ((DynamicRegistryImpl<?>) configurationRegistry(registries, packet.registryId()))
                .applyRegistryDataPacket(registries,
                        configurationRegistry(VanillaHolder.REGISTRIES, packet.registryId()), packet);
    }

    static DynamicRegistry<?> configurationRegistry(Registries registries, String registryId) {
        for (DynamicRegistry<?> registry : configurationRegistries(registries)) {
            if (registry.key().asString().equals(registryId))
                return registry;
        }
        throw new IllegalArgumentException("Unknown registry data packet registry: " + registryId);
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
                registries.dimensionType()
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

    private static final class VanillaHolder {
        private static final Registries REGISTRIES = Registries.vanilla();
    }
}
