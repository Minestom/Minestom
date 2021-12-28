package net.minestom.server.tags;

import java.util.*;

import net.minestom.server.entity.EntityType;
import net.minestom.server.fluid.Fluid;
import net.minestom.server.gameevent.GameEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * A type of a {@link GameTag tag}.
 *
 * @param <T> the type
 */
public final class GameTagType<T extends ProtocolObject> {

    public static final GameTagType<Block> BLOCKS = new GameTagType<>("minecraft:block", Registry.Resource.BLOCK_TAGS,
            name -> Objects.requireNonNull(Block.fromNamespaceId(name)), id -> Objects.requireNonNull(Block.fromBlockId(id)));
    public static final GameTagType<Material> ITEMS = new GameTagType<>("minecraft:item", Registry.Resource.ITEM_TAGS,
            name -> Objects.requireNonNull(Material.fromNamespaceId(name)), id -> Objects.requireNonNull(Material.fromId(id)));
    public static final GameTagType<Fluid> FLUIDS = new GameTagType<>("minecraft:fluid", Registry.Resource.FLUID_TAGS,
            name -> Objects.requireNonNull(Registries.getFluid(name)), id -> Objects.requireNonNull(Fluid.fromId((short) id)));
    public static final GameTagType<EntityType> ENTITY_TYPES = new GameTagType<>("minecraft:entity_type",
            Registry.Resource.ENTITY_TYPE_TAGS, name -> Objects.requireNonNull(EntityType.fromNamespaceId(name)),
            id -> Objects.requireNonNull(EntityType.fromId(id)));
    public static final GameTagType<GameEvent> GAME_EVENTS = new GameTagType<>("minecraft:game_event",
            Registry.Resource.GAMEPLAY_TAGS, name -> Objects.requireNonNull(GameEvent.fromNamespaceId(name)),
            id -> Objects.requireNonNull(GameEvent.fromId(id)));

    private static final List<GameTagType<? extends ProtocolObject>> VALUES = new ArrayList<>(Arrays.asList(BLOCKS, ITEMS, FLUIDS, ENTITY_TYPES, GAME_EVENTS));

    public static @Nullable GameTagType<? extends ProtocolObject> fromIdentifier(final @NotNull String identifier) {
        for (final GameTagType<?> type : VALUES) {
            if (type.identifier().equals(identifier)) return type;
        }
        return null;
    }

    public static @NotNull List<@NotNull GameTagType<? extends ProtocolObject>> values() {
        return Collections.unmodifiableList(VALUES);
    }

    /**
     * Adds the given type to the list of available tag types.
     *
     * @param type the type
     */
    public static void add(final @NotNull GameTagType<? extends ProtocolObject> type) {
        VALUES.add(type);
    }

    private final String identifier;
    private final Registry.Resource resource;
    private final Function<String, T> fromName;
    private final IntFunction<T> fromId;

    public GameTagType(final @NotNull String identifier,
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
}
