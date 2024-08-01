package net.minestom.server.gamedata.tags;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Handles loading and caching of tags.
 */
public final class TagManager {
    private final Map<Tag.BasicType, List<Tag>> tagMap = new ConcurrentHashMap<>();

    public TagManager() {
        // Load required tags from files
        for (var type : Tag.BasicType.values()) {
            if (type.getResource() == null || type.getFunction() == null) continue;
            final var json = Registry.load(type.getResource());
            final var tagIdentifierMap = tagMap.computeIfAbsent(type, s -> new CopyOnWriteArrayList<>());
            json.keySet().forEach(tagName -> {
                final var tag = new Tag(Key.key(tagName), getValues(json, tagName));
                tagIdentifierMap.add(tag);
            });
        }
    }

    public @Nullable Tag getTag(Tag.BasicType type, String key) {
        final var tags = tagMap.get(type);
        for (var tag : tags) {
            if (tag.key().asString().equals(key))
                return tag;
        }
        return null;
    }

    public Map<Tag.BasicType, List<Tag>> getTagMap() {
        return Collections.unmodifiableMap(tagMap);
    }

    private Set<Key> getValues(Map<String, Map<String, Object>> main, String value) {
        Map<String, Object> tagObject = main.get(value);
        final List<String> tagValues = (List<String>) tagObject.get("values");
        Set<Key> result = new HashSet<>(tagValues.size());
        tagValues.forEach(tagString -> {
            if (tagString.startsWith("#")) {
                result.addAll(getValues(main, tagString.substring(1)));
            } else {
                result.add(Key.key(tagString));
            }
        });
        return result;
    }
}
