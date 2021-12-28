package net.minestom.server.tags;

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

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * A type of {@link GameTag tag}.
 *
 * @param <T> the type
 */
public record GameTagType<T extends ProtocolObject>(@NotNull String identifier,
                                                    @NotNull Registry.Resource resource,
                                                    @NotNull Function<@NotNull String, @NotNull T> fromName,
                                                    @NotNull IntFunction<@NotNull T> fromId) {
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

    private static final List<GameTagType<? extends ProtocolObject>> VALUES = List.of(BLOCKS, ITEMS, FLUIDS, ENTITY_TYPES, GAME_EVENTS);

    public static @Nullable GameTagType<? extends ProtocolObject> fromIdentifier(final @NotNull String identifier) {
        for (final GameTagType<?> type : VALUES) {
            if (type.identifier().equals(identifier)) return type;
        }
        return null;
    }

    public static @NotNull List<@NotNull GameTagType<? extends ProtocolObject>> values() {
        return VALUES;
    }
}
