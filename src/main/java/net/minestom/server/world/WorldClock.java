package net.minestom.server.world;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.ApiStatus;

public sealed interface WorldClock extends WorldClocks permits WorldClockImpl {
    NetworkBuffer.Type<RegistryKey<WorldClock>> NETWORK_TYPE = RegistryKey.networkType(Registries::worldClock);
    Codec<RegistryKey<WorldClock>> CODEC = RegistryKey.codec(Registries::worldClock);

    Codec<WorldClock> REGISTRY_CODEC = StructCodec.struct(WorldClock::create);

    static WorldClock create() {
        return new WorldClockImpl();
    }

    /**
     * Creates a new instance of the "minecraft:world_clock" registry containing the vanilla contents.
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static DynamicRegistry<WorldClock> createDefaultRegistry() {
        return DynamicRegistry.create(Key.key("world_clock"), REGISTRY_CODEC, RegistryData.Resource.WORLD_CLOCKS);
    }
}
