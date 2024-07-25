package net.minestom.server.statistic;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.StaticProtocolObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public sealed interface StatisticType extends StaticProtocolObject, StatisticTypes permits StatisticTypeImpl {

    static @NotNull Collection<@NotNull StatisticType> values() {
        return StatisticTypeImpl.values();
    }

    static @Nullable StatisticType fromKey(@NotNull String key) {
        return StatisticTypeImpl.getSafe(key);
    }

    static @Nullable StatisticType fromKey(@NotNull Key key) {
        return fromKey(key.asString());
    }

    static @Nullable StatisticType fromId(int id) {
        return StatisticTypeImpl.getId(id);
    }
}
