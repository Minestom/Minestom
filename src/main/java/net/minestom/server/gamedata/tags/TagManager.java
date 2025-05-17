package net.minestom.server.gamedata.tags;

import net.kyori.adventure.key.Key;
import net.minestom.server.network.packet.server.common.TagsPacket;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Handles loading and caching of tags.
 */
public final class TagManager {
    private final Map<Tag.BasicType, List<Tag>> tagMap;

    public TagManager() {
        // Load required tags from files
        this.tagMap = Arrays.stream(Tag.BasicType.values())
                .filter(type -> type.getResource() != null && type.getFunction() != null)
                .map(type -> {
                    var json = Registry.load(type.getResource());
                    if (json.isEmpty()) return null;
                    var tags = json.keySet().stream()
                                    .map(tagName -> new Tag(Key.key(tagName), getValues(json, tagName)))
                                    .toList();
                    // Create a list copy because we already know there should be no nulls.
                    return Map.entry(type, List.copyOf(tags));
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existing, incoming) -> List.copyOf(
                                Stream.concat(existing.stream(), incoming.stream()).toList()
                        )
                ));

    }

    public @Nullable Tag getTag(Tag.BasicType type, String namespace) {
        final var tags = tagMap.get(type);
        for (final var tag : tags) {
            if (tag.name().equals(namespace))
                return tag;
        }
        return null;
    }

    public @Unmodifiable Map<Tag.BasicType, @Unmodifiable List<Tag>> getTagMap() {
        return tagMap;
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
        return Set.copyOf(result);
    }
}
