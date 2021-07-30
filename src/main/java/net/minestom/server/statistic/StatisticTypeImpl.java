package net.minestom.server.statistic;

import com.google.gson.JsonObject;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

final class StatisticTypeImpl implements StatisticType {
    private static final Registry.Loader<StatisticType> LOADER = new Registry.Loader<>();

    static StatisticType get(@NotNull String namespace) {
        return LOADER.get(namespace);
    }

    static StatisticType getSafe(@NotNull String namespace) {
        return LOADER.getSafe(namespace);
    }

    static StatisticType getId(int id) {
        return LOADER.getId(id);
    }

    static Collection<StatisticType> values() {
        return LOADER.values();
    }

    static {
        // Load data from file
        JsonObject statistics = Registry.load(Registry.Resource.STATISTICS);
        statistics.entrySet().forEach(entry -> {
            final String namespace = entry.getKey();
            final JsonObject object = entry.getValue().getAsJsonObject();
            final int id = object.get("id").getAsInt();
            LOADER.register(new StatisticTypeImpl(NamespaceID.from(namespace), id));
        });
    }

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
