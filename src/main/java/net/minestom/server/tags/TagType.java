package net.minestom.server.tags;

import java.util.*;

import net.minestom.server.entity.EntityType;
import net.minestom.server.fluid.Fluid;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.gameevent.GameEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * A type of a {@link net.minestom.server.tags.Tag tag}.
 *
 * @param <T> the type
 */
public final class TagType<T extends ProtocolObject> {

    public static final TagType<Block> BLOCKS = new TagType<>("minecraft:block", Registry.Resource.BLOCK_TAGS,
            name -> Objects.requireNonNull(Block.fromNamespaceId(name)), id -> Objects.requireNonNull(Block.fromBlockId(id)));
    public static final TagType<Material> ITEMS = new TagType<>("minecraft:item", Registry.Resource.ITEM_TAGS,
            name -> Objects.requireNonNull(Material.fromNamespaceId(name)), id -> Objects.requireNonNull(Material.fromId(id)));
    public static final TagType<Fluid> FLUIDS = new TagType<>("minecraft:fluid", Registry.Resource.FLUID_TAGS,
            name -> Objects.requireNonNull(Registries.getFluid(name)), id -> Objects.requireNonNull(Fluid.fromId((short) id)));
    public static final TagType<EntityType> ENTITY_TYPES = new TagType<>("minecraft:entity_type",
            Registry.Resource.ENTITY_TYPE_TAGS, name -> Objects.requireNonNull(EntityType.fromNamespaceId(name)),
            id -> Objects.requireNonNull(EntityType.fromId(id)));
    public static final TagType<GameEvent> GAME_EVENTS = new TagType<>("minecraft:game_event",
            Registry.Resource.GAMEPLAY_TAGS, name -> Objects.requireNonNull(GameEvent.fromNamespaceId(name)),
            id -> Objects.requireNonNull(GameEvent.fromId(id)));

    private static final List<TagType<? extends ProtocolObject>> VALUES = new ArrayList<>(Arrays.asList(BLOCKS, ITEMS, FLUIDS, ENTITY_TYPES, GAME_EVENTS));

    public static @Nullable TagType<? extends ProtocolObject> fromIdentifier(final @NotNull String identifier) {
        for (final TagType<?> type : VALUES) {
            if (type.identifier().equals(identifier)) return type;
        }
        return null;
    }

    public static List<TagType<? extends ProtocolObject>> values() {
        return Collections.unmodifiableList(VALUES);
    }

    /**
     * Adds the given type to the list of available tag types.
     *
     * @param type the type
     */
    public static void add(final TagType<? extends ProtocolObject> type) {
        VALUES.add(type);
    }

    private final String identifier;
    private final Registry.Resource resource;
    private final Function<String, T> fromName;
    private final IntFunction<T> fromId;

    public TagType(final @NotNull String identifier,
                   final @NotNull Registry.Resource resource,
                   final @NotNull Function<@NotNull String, @NotNull T> fromName,
                   final @NotNull IntFunction<@NotNull T> fromId) {
        this.identifier = identifier;
        this.resource = resource;
        this.fromName = fromName;
        this.fromId = fromId;
    }

    public @NotNull String identifier() {
        return identifier;
    }

    public @NotNull Registry.Resource resource() {
        return resource;
    }

    public @NotNull Function<@NotNull String, @NotNull T> fromName() {
        return fromName;
    }

    public @NotNull IntFunction<@NotNull T> fromId() {
        return fromId;
    }

    /**
     * WARNING: Subject for removal at any time! If you use this, and it gets removed,
     * that's on you.
     */
    @Deprecated
    @ApiStatus.Internal
    public @Nullable Tag.BasicType legacy() {
        if (this == TagType.BLOCKS) {
            return Tag.BasicType.BLOCKS;
        } else if (this == TagType.ITEMS) {
            return Tag.BasicType.ITEMS;
        } else if (this == TagType.FLUIDS) {
            return Tag.BasicType.FLUIDS;
        } else if (this == TagType.ENTITY_TYPES) {
            return Tag.BasicType.ENTITY_TYPES;
        } else if (this == TagType.GAME_EVENTS) {
            return Tag.BasicType.GAME_EVENTS;
        } else {
            return null;
        }
    }
}
