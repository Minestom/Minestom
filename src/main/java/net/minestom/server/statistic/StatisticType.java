package net.minestom.server.statistic;

import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

@ApiStatus.NonExtendable
public interface StatisticType extends ProtocolObject, StatisticTypes {

    static @NotNull Collection<@NotNull StatisticType> values() {
        return StatisticTypeImpl.values();
    }

    static @Nullable StatisticType fromNamespaceId(@NotNull String namespaceID) {
        return StatisticTypeImpl.getSafe(namespaceID);
    }

    static @Nullable StatisticType fromNamespaceId(@NotNull NamespaceID namespaceID) {
        return fromNamespaceId(namespaceID.asString());
    }

    static @Nullable StatisticType fromId(int id) {
        return StatisticTypeImpl.getId(id);
    }
}
