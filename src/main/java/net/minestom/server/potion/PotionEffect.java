package net.minestom.server.potion;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.kyori.adventure.translation.Translatable;
import net.minestom.server.codec.Codec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryData;
import net.minestom.server.registry.StaticProtocolObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public sealed interface PotionEffect extends StaticProtocolObject<PotionEffect>, PotionEffects,
        Translatable permits PotionEffectImpl {
    NetworkBuffer.Type<PotionEffect> NETWORK_TYPE = NetworkBuffer.VAR_INT.transform(PotionEffect::fromId, PotionEffect::id);
    Codec<PotionEffect> CODEC = Codec.KEY.transform(PotionEffect::fromKey, PotionEffect::key);

    /**
     * Returns the legacy registry data backing this potion effect.
     *
     * @return the legacy registry data
     * @deprecated use the direct accessors on {@link PotionEffect}
     */
    @Deprecated(forRemoval = true)
    @Contract(pure = true)
    RegistryData.PotionEffectEntry registry();

    @Override
    default Key key() {
        return registry().key();
    }

    @Override
    default int id() {
        return registry().id();
    }

    @Override
    default String translationKey() {
        return registry().translationKey();
    }

    /**
     * Returns the display color of this potion effect.
     *
     * @return the RGB color
     */
    @Contract(pure = true)
    default int color() {
        return registry().color();
    }

    /**
     * Returns whether this potion effect applies instantaneously.
     *
     * @return {@code true} if this effect is instantaneous
     */
    @Contract(pure = true)
    default boolean instantaneous() {
        return registry().isInstantaneous();
    }

    static Collection<PotionEffect> values() {
        return PotionEffectImpl.REGISTRY.values();
    }

    static @Nullable PotionEffect fromKey(@KeyPattern String key) {
        return fromKey(Key.key(key));
    }

    static @Nullable PotionEffect fromKey(Key key) {
        return PotionEffectImpl.REGISTRY.get(key);
    }

    static @Nullable PotionEffect fromId(int id) {
        return PotionEffectImpl.REGISTRY.get(id);
    }

    static Registry<PotionEffect> staticRegistry() {
        return PotionEffectImpl.REGISTRY;
    }
}
