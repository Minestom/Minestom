package net.minestom.server.tags;

import java.util.*;

import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;

public final class GameTagManager {
    private final Map<GameTagType<?>, Set<GameTag<?>>> tagsByType = new ConcurrentHashMap<>();

    public GameTagManager() {
        // Load required tags from files
        for (final var type : GameTagType.values()) {
            final var json = Registry.load(type.resource());
            final var tags = tagsByType.computeIfAbsent(type, s -> ConcurrentHashMap.newKeySet());
            for (final var key : json.keySet()) {
                final var tag = new GameTag<>(NamespaceID.from(key), type, getKeys(json, key));
                tags.add(tag);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends ProtocolObject> @Nullable GameTag<T> get(final @NotNull GameTagType<T> type, final @NotNull String namespace) {
        final var tags = tagsByType.get(type);
        for (final var tag : tags) {
            if (tag.name().asString().equals(namespace)) return (GameTag<T>) tag;
        }
        return null;
    }

    public @NotNull Map<@NotNull GameTagType<?>, @NotNull Set<@NotNull GameTag<?>>> getTags() {
        return Collections.unmodifiableMap(tagsByType);
    }

    @SuppressWarnings("unchecked")
    private static Set<String> getKeys(final @NotNull Map<String, Map<String, Object>> main, final @NotNull String value) {
        final Map<String, Object> tagObject = main.get(value);
        final var tagValues = (List<String>) tagObject.get("values");
        final Set<String> result = new HashSet<>(tagValues.size());
        for (final String element : tagValues) {
            if (element.startsWith("#")) {
                result.addAll(getKeys(main, element.substring(1)));
            } else {
                result.add(element);
            }
        }
        return result;
    }
}
