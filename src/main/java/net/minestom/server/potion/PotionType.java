package net.minestom.server.potion;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.minestom.server.codec.Codec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.StaticProtocolObject;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

public sealed interface PotionType extends StaticProtocolObject<PotionType>, PotionTypes permits PotionTypeImpl {

    NetworkBuffer.Type<PotionType> NETWORK_TYPE = NetworkBuffer.VAR_INT.transform(PotionType::fromId, PotionType::id);
    Codec<PotionType> CODEC = Codec.KEY.transform(PotionType::fromKey, PotionType::key);

    static Collection<PotionType> values() {
        return PotionTypeImpl.REGISTRY.values();
    }

    static @Nullable PotionType fromKey(@KeyPattern String key) {
        return fromKey(Key.key(key));
    }

    static @Nullable PotionType fromKey(Key key) {
        return PotionTypeImpl.REGISTRY.get(key);
    }

    static @Nullable PotionType fromId(int id) {
        return PotionTypeImpl.REGISTRY.get(id);
    }
}
