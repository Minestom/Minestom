package net.minestom.server.potion;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.minestom.server.codec.Codec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.StaticProtocolObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public sealed interface PotionEffect extends StaticProtocolObject<PotionEffect>, PotionEffects permits PotionEffectImpl {
    @NotNull NetworkBuffer.Type<PotionEffect> NETWORK_TYPE = NetworkBuffer.VAR_INT.transform(PotionEffect::fromId, PotionEffect::id);
    @NotNull Codec<PotionEffect> CODEC = Codec.KEY.transform(PotionEffect::fromKey, PotionEffect::key);

    @Contract(pure = true)
    @NotNull RegistryData.PotionEffectEntry registry();

    @Override
    default @NotNull Key key() {
        return registry().key();
    }

    @Override
    default int id() {
        return registry().id();
    }

    static @NotNull Collection<@NotNull PotionEffect> values() {
        return PotionEffectImpl.REGISTRY.values();
    }

    static @Nullable PotionEffect fromKey(@KeyPattern @NotNull String key) {
        return fromKey(Key.key(key));
    }

    static @Nullable PotionEffect fromKey(@NotNull Key key) {
        return PotionEffectImpl.REGISTRY.get(key);
    }

    static @Nullable PotionEffect fromId(int id) {
        return PotionEffectImpl.REGISTRY.get(id);
    }

    static @NotNull Registry<PotionEffect> staticRegistry() {
        return PotionEffectImpl.REGISTRY;
    }
}
