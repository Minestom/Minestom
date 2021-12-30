package net.minestom.server.gamedata.tags;

import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.tags.GameTag;
import net.minestom.server.tags.GameTagType;
import net.minestom.server.tags.GameTags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles loading and caching of tags.
 *
 * @deprecated for removal, replaced with tag holder objects and game tag
 * constants
 */
@Deprecated(forRemoval = true)
public final class TagManager {

    public @Nullable Tag getTag(Tag.BasicType type, String namespace) {
        return convertTag(GameTags.get(Objects.requireNonNull(GameTagType.fromIdentifier(type.getIdentifier())), namespace));
    }

    public @NotNull Map<Tag.BasicType, List<Tag>> getTagMap() {
        final Map<Tag.BasicType, List<Tag>> tags = new HashMap<>();
        for (final var entry : GameTags.tags().entrySet()) {
            final var converted = entry.getValue().stream()
                    .map(TagManager::convertTag)
                    .collect(Collectors.toList());
            tags.put(Tag.BasicType.fromIdentifer(entry.getKey().identifier().asString()), converted);
        }
        return Collections.unmodifiableMap(tags);
    }

    private static <T extends ProtocolObject> @Nullable Tag convertTag(final @Nullable GameTag<T> tag) {
        if (tag == null) return null;
        final var values = tag.values().stream()
                .map(ProtocolObject::namespace)
                .collect(Collectors.toUnmodifiableSet());
        return new Tag(tag.name(), values);
    }
}
