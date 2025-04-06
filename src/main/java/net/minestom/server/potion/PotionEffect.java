package net.minestom.server.potion;

import net.kyori.adventure.key.Key;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.StaticProtocolObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public sealed interface PotionEffect extends StaticProtocolObject, PotionEffects permits PotionEffectImpl {

    NetworkBuffer.Type<PotionEffect> NETWORK_TYPE = NetworkBuffer.VAR_INT.transform(PotionEffectImpl::getId, PotionEffect::id);

    @Contract(pure = true)
    @NotNull Registry.PotionEffectEntry registry();

    @Override
    default @NotNull Key key() {
        return registry().key();
    }

    @Override
    default int id() {
        return registry().id();
    }

    static @NotNull Collection<@NotNull PotionEffect> values() {
        return PotionEffectImpl.values();
    }

    static @Nullable PotionEffect fromKey(@NotNull String key) {
        return PotionEffectImpl.getSafe(key);
    }

    static @Nullable PotionEffect fromKey(@NotNull Key key) {
        return fromKey(key.asString());
    }

    static @Nullable PotionEffect fromId(int id) {
        return PotionEffectImpl.getId(id);
    }
}
