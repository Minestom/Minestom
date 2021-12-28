package net.minestom.server.gamedata.tags;

import net.minestom.server.MinecraftServer;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.tags.GameTag;
import net.minestom.server.tags.GameTagManager;
import net.minestom.server.tags.GameTagType;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles loading and caching of tags.
 *
 * @deprecated for removal, replaced with {@link net.minestom.server.tags.GameTagManager}
 */
@Deprecated(forRemoval = true)
public final class TagManager {
    private final GameTagManager gameTagManager;

    public TagManager() {
        gameTagManager = MinecraftServer.getGameTagManager();
    }

    @Deprecated(forRemoval = true)
    public @Nullable Tag getTag(Tag.BasicType type, String namespace) {
        return convertTag(gameTagManager.get(Objects.requireNonNull(GameTagType.fromIdentifier(type.getIdentifier())), namespace));
    }

    @Deprecated(forRemoval = true)
    public Map<Tag.BasicType, List<Tag>> getTagMap() {
        final Map<Tag.BasicType, List<Tag>> tags = new HashMap<>();
        for (final var entry : gameTagManager.getTags().entrySet()) {
            final var converted = entry.getValue().stream()
                    .map(TagManager::convertTag)
                    .collect(Collectors.toList());
            tags.put(Tag.BasicType.fromIdentifer(entry.getKey().identifier()), converted);
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
