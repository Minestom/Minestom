package net.minestom.server.gamedata.tags;

import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
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
            final var json = Registry.load(type.getResource());
            final var tagIdentifierMap = tagMap.computeIfAbsent(type, s -> new CopyOnWriteArrayList<>());
            json.forEach((namespace, main) -> {
                final var tag = new Tag(NamespaceID.from(namespace), getValues(main, namespace));
                tagIdentifierMap.add(tag);
            });
        }
    }

    public @Nullable Tag getTag(Tag.BasicType type, String namespace) {
        final var tags = tagMap.get(type);
        for (var tag : tags) {
            if (tag.getName().asString().equals(namespace))
                return tag;
        }
        return null;
    }

    public Map<Tag.BasicType, List<Tag>> getTagMap() {
        return Collections.unmodifiableMap(tagMap);
    }

    private Set<NamespaceID> getValues(Map<String, Object> main, String value) {
        if (true) return new HashSet<>(); // TODO
        Map<String, Object> tagObject = (Map<String, Object>) main.get(value);
        final List<Object> tagValues = (List<Object>) tagObject.get("values");
        Set<NamespaceID> result = new HashSet<>(tagValues.size());
        for (Object tagValue : tagValues) {
            final String tagString = (String) tagValue;
            if (tagString.startsWith("#")) {
                result.addAll(getValues(main, tagString.substring(1)));
            } else {
                result.add(NamespaceID.from(tagString));
            }
        }
        return result;
    }
}
