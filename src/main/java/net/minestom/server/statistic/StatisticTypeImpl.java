package net.minestom.server.statistic;

import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

final class StatisticTypeImpl implements StatisticType {
    private final NamespaceID namespaceID;
    private final int id;

    StatisticTypeImpl(NamespaceID namespaceID, int id) {
        this.namespaceID = namespaceID;
        this.id = id;
    }

    @Override
    public @NotNull NamespaceID namespace() {
        return namespaceID;
    }

    @Override
    public int id() {
        return id;
    }
}
