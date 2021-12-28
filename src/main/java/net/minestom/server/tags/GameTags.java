package net.minestom.server.tags;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minestom.server.entity.EntityType;
import net.minestom.server.fluid.Fluid;
import net.minestom.server.gameevent.GameEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Internal use only. Do not use.
 */
@ApiStatus.Internal
public final class GameTags {

    public static final @NotNull Set<GameTag<Block>> BLOCKS = load(GameTagType.BLOCKS);
    public static final @NotNull Set<GameTag<Material>> ITEMS = load(GameTagType.ITEMS);
    public static final @NotNull Set<GameTag<Fluid>> FLUIDS = load(GameTagType.FLUIDS);
    public static final @NotNull Set<GameTag<EntityType>> ENTITY_TYPES = load(GameTagType.ENTITY_TYPES);
    public static final @NotNull Set<GameTag<GameEvent>> GAME_EVENTS = load(GameTagType.GAME_EVENTS);
    private static final Map<GameTagType<?>, Set<? extends GameTag<?>>> TAGS_BY_TYPE = Map.of(
            GameTagType.BLOCKS, BLOCKS,
            GameTagType.ITEMS, ITEMS,
            GameTagType.FLUIDS, FLUIDS,
            GameTagType.ENTITY_TYPES, ENTITY_TYPES,
            GameTagType.GAME_EVENTS, GAME_EVENTS
    );

    @SuppressWarnings("unchecked")
    public static @NotNull Map<GameTagType<?>, Set<GameTag<?>>> tags() {
        return (Map<GameTagType<?>, Set<GameTag<?>>>) (Object) TAGS_BY_TYPE;
    }

    @SuppressWarnings("unchecked")
    public static <T extends ProtocolObject> GameTag<T> get(final @NotNull GameTagType<T> type, final @NotNull String key) {
        final Set<? extends GameTag<?>> tags = TAGS_BY_TYPE.get(type);
        for (final var tag : tags) {
            if (tag.name().asString().equals(key)) return (GameTag<T>) tag;
        }
        throw new IllegalStateException("No tag found for key '" + key + "' with type '" + type.identifier() + "'!");
    }

    private static <T extends ProtocolObject> @NotNull Set<GameTag<T>> load(final @NotNull GameTagType<T> type) {
        final var json = Registry.load(type.resource());
        final Set<GameTag<T>> tags = new HashSet<>();
        for (final var key : json.keySet()) {
            final var tag = new GameTag<>(NamespaceID.from(key), type, keys(json, key));
            tags.add(tag);
        }
        return tags;
    }

    @SuppressWarnings("unchecked")
    private static Set<String> keys(final @NotNull Map<String, Map<String, Object>> main, final @NotNull String value) {
        final var tagObject = main.get(value);
        final var tagValues = (List<String>) tagObject.get("values");
        final Set<String> result = new HashSet<>(tagValues.size());
        for (final String element : tagValues) {
            if (element.startsWith("#")) {
                result.addAll(keys(main, element.substring(1)));
            } else {
                result.add(element);
            }
        }
        return result;
    }

    private GameTags() {
    }
}
