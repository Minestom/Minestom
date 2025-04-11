package net.minestom.server.statistic;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.minestom.server.registry.StaticProtocolObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public sealed interface StatisticType extends StaticProtocolObject, StatisticTypes permits StatisticTypeImpl {

    static @NotNull Collection<@NotNull StatisticType> values() {
        return StatisticTypeImpl.REGISTRY.values();
    }

    static @Nullable StatisticType fromKey(@KeyPattern @NotNull String key) {
        return fromKey(Key.key(key));
    }

    static @Nullable StatisticType fromKey(@NotNull Key key) {
        return StatisticTypeImpl.REGISTRY.get(key);
    }

    static @Nullable StatisticType fromId(int id) {
        return StatisticTypeImpl.REGISTRY.get(id);
    }

}
