package net.minestom.server.gamedata.tags;

import net.kyori.adventure.key.Key;
import net.minestom.server.network.packet.server.common.TagsPacket;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryData;
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
            final var json = RegistryData.load(type.getResource());
            final var tagIdentifierMap = tagMap.computeIfAbsent(type, s -> new CopyOnWriteArrayList<>());
            json.keySet().forEach(tagName -> {
                final var tag = new Tag(Key.key(tagName), getValues(json, tagName));
                tagIdentifierMap.add(tag);
            });
        }
    }

    public @Nullable Tag getTag(Tag.BasicType type, String namespace) {
        final var tags = tagMap.get(type);
        for (final var tag : tags) {
            if (tag.name().equals(namespace))
                return tag;
        }
        return null;
    }

    public Map<Tag.BasicType, List<Tag>> getTagMap() {
        return Collections.unmodifiableMap(tagMap);
    }

    public TagsPacket packet(Registries registries) {
        List<TagsPacket.Registry> registryList = new ArrayList<>();
        for (Map.Entry<Tag.BasicType, List<Tag>> entry : tagMap.entrySet()) {
            final Tag.BasicType type = entry.getKey();
            final String registry = type.getIdentifier();
            final List<TagsPacket.Tag> tags = new ArrayList<>();
            for (final Tag tag : entry.getValue()) {
                final String identifier = tag.name();
                final int[] values = tag.getValues().stream().mapToInt(value -> type.getFunction().apply(value.asString(), registries).orElse(null)).filter(Objects::nonNull).toArray();
                tags.add(new TagsPacket.Tag(identifier, values));
            }
            registryList.add(new TagsPacket.Registry(registry, tags));
        }
        return new TagsPacket(registryList);
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
