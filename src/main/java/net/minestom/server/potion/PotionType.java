package net.minestom.server.potion;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.minestom.server.codec.Codec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.StaticProtocolObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public sealed interface PotionType extends StaticProtocolObject<PotionType>, PotionTypes permits PotionTypeImpl {

    @NotNull NetworkBuffer.Type<PotionType> NETWORK_TYPE = NetworkBuffer.VAR_INT.transform(PotionType::fromId, PotionType::id);
    @NotNull Codec<PotionType> CODEC = Codec.KEY.transform(PotionType::fromKey, PotionType::key);

    static @NotNull Collection<@NotNull PotionType> values() {
        return PotionTypeImpl.REGISTRY.values();
    }

    static @Nullable PotionType fromKey(@KeyPattern @NotNull String key) {
        return fromKey(Key.key(key));
    }

    static @Nullable PotionType fromKey(@NotNull Key key) {
        return PotionTypeImpl.REGISTRY.get(key);
    }

    static @Nullable PotionType fromId(int id) {
        return PotionTypeImpl.REGISTRY.get(id);
    }

    static @NotNull Registry<PotionType> staticRegistry() {
        return PotionTypeImpl.REGISTRY;
    }
}
