package net.minestom.server.instance.block.pot;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.BuiltinRegistries;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Holder;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.ApiStatus;

public sealed interface DecoratedPotPattern extends Holder.Direct<DecoratedPotPattern>, DecoratedPotPatterns permits DecoratedPotPatternImpl {
    Codec<DecoratedPotPattern> REGISTRY_CODEC = StructCodec.struct(
            "asset_id", Codec.KEY, DecoratedPotPattern::assetId,
            DecoratedPotPattern::create);

    NetworkBuffer.Type<RegistryKey<DecoratedPotPattern>> NETWORK_TYPE = RegistryKey.networkType(Registries::decoratedPotPattern);
    Codec<RegistryKey<DecoratedPotPattern>> CODEC = RegistryKey.codec(Registries::decoratedPotPattern);

    static DecoratedPotPattern create(Key assetId) {
        return new DecoratedPotPatternImpl(assetId);
    }

    /**
     * <p>Creates a new registry for decorated pot patterns, loading the vanilla patterns.</p>
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static DynamicRegistry<DecoratedPotPattern> createDefaultRegistry() {
        return DynamicRegistry.create(BuiltinRegistries.DECORATED_POT_PATTERN, REGISTRY_CODEC);
    }

    Key assetId();
}
