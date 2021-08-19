package net.minestom.server.gamedata.tags;

import com.google.gson.JsonObject;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registry;
import net.minestom.server.tags.TagType;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Handles loading and caching of tags.
 */
public final class TagManager {
    private final Map<TagType<? extends ProtocolObject>, List<net.minestom.server.tags.Tag<? extends ProtocolObject>>> tagMap = new ConcurrentHashMap<>();

    public TagManager() {
        // Load required tags from files
        for (var type : TagType.values()) {
            final var json = Registry.load(type.resource());
            final var tagIdentifierMap = tagMap.computeIfAbsent(type, s -> new CopyOnWriteArrayList<>());
            json.keySet().forEach(tagName -> {
                final var tag = new net.minestom.server.tags.Tag<>(NamespaceID.from(tagName), type, getKeys(json, tagName));
                tagIdentifierMap.add(tag);
            });
        }
    }

    @SuppressWarnings("unchecked")
    public @Nullable <T extends ProtocolObject> net.minestom.server.tags.Tag<T> get(final @NotNull TagType<T> type, final @NotNull String namespace) {
        final var tags = tagMap.get(type);
        for (final var tag : tags) {
            if (tag.name().asString().equals(namespace)) return (net.minestom.server.tags.Tag<T>) tag;
        }
        return null;
    }

    public Map<TagType<? extends ProtocolObject>, List<net.minestom.server.tags.Tag<? extends ProtocolObject>>> getTags() {
        return Collections.unmodifiableMap(tagMap);
    }

    @Deprecated
    public @Nullable Tag getTag(Tag.BasicType type, String namespace) {
        final var tags = tagMap.get(type);
        for (var tag : tags) {
            if (tag.name().asString().equals(namespace)) {
                final Set<NamespaceID> names = new HashSet<>();
                tag.values().forEach(object -> names.add(object.namespace()));
                return new Tag(tag.name(), names);
            }
        }
        return null;
    }

    @Deprecated
    public Map<Tag.BasicType, List<Tag>> getTagMap() {
        final Map<Tag.BasicType, List<Tag>> map = new HashMap<>();
        for (final var entry : tagMap.entrySet()) {
            final Tag.BasicType legacy = entry.getKey().legacy();
            if (legacy != null) {
                final List<Tag> values = new ArrayList<>();
                entry.getValue().forEach(tag -> {
                    final Set<NamespaceID> names = new HashSet<>();
                    tag.values().forEach(object -> names.add(object.namespace()));
                    values.add(new Tag(tag.name(), names));
                });
                map.put(legacy, values);
            }
        }
        return Collections.unmodifiableMap(map);
    }

    private Set<String> getKeys(final JsonObject main, final String value) {
        final JsonObject tagObject = main.getAsJsonObject(value);
        final var tagValues = tagObject.getAsJsonArray("values");
        final Set<String> result = new HashSet<>(tagValues.size());
        tagValues.forEach(jsonElement -> {
            final String tagString = jsonElement.getAsString();
            if (tagString.startsWith("#")) {
                result.addAll(getKeys(main, tagString.substring(1)));
            } else {
                result.add(tagString);
            }
        });
        return result;
    }
}
