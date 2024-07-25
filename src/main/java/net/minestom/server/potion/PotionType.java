package net.minestom.server.potion;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.StaticProtocolObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public sealed interface PotionType extends StaticProtocolObject, PotionTypes permits PotionTypeImpl {

    static @NotNull Collection<@NotNull PotionType> values() {
        return PotionTypeImpl.values();
    }

    static @Nullable PotionType fromKey(@NotNull String key) {
        return PotionTypeImpl.getSafe(key);
    }

    static @Nullable PotionType fromKey(@NotNull Key key) {
        return fromKey(key.asString());
    }

    static @Nullable PotionType fromId(int id) {
        return PotionTypeImpl.getId(id);
    }
}
