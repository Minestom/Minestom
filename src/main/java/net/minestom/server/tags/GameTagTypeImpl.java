package net.minestom.server.tags;

import java.util.Set;
import java.util.function.Function;

import java.util.function.IntFunction;

import net.minestom.server.entity.EntityType;
import net.minestom.server.fluid.Fluid;
import net.minestom.server.gameevent.GameEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

record GameTagTypeImpl<T extends ProtocolObject>(@NotNull NamespaceID identifier,
                                                 @NotNull Registry.Resource resource,
                                                 @NotNull Function<@NotNull String, @NotNull T> fromName,
                                                 @NotNull IntFunction<@NotNull T> fromId) implements GameTagType<T> {
    static final GameTagType<Block> BLOCKS = new GameTagTypeImpl<>("block", Registry.Resource.BLOCK_TAGS,
            Block::fromNamespaceId, Block::fromBlockId);
    static final GameTagType<Material> ITEMS = new GameTagTypeImpl<>("item", Registry.Resource.ITEM_TAGS,
            Material::fromNamespaceId, Material::fromId);
    static final GameTagType<Fluid> FLUIDS = new GameTagTypeImpl<>("fluid", Registry.Resource.FLUID_TAGS,
            Registries::getFluid, id -> Fluid.fromId((short) id));
    static final GameTagType<EntityType> ENTITY_TYPES = new GameTagTypeImpl<>("entity_type", Registry.Resource.ENTITY_TYPE_TAGS,
            EntityType::fromNamespaceId, EntityType::fromId);
    static final GameTagType<GameEvent> GAME_EVENTS = new GameTagTypeImpl<>("game_event", Registry.Resource.GAMEPLAY_TAGS,
            GameEvent::fromNamespaceId, GameEvent::fromId);

    static final Set<GameTagType<?>> VALUES = Set.of(BLOCKS, ITEMS, FLUIDS, ENTITY_TYPES, GAME_EVENTS);

    static @Nullable GameTagType<?> fromIdentifier(final @NotNull String identifier) {
        for (final GameTagType<?> type : VALUES) {
            if (type.identifier().asString().equals(identifier)) return type;
        }
        return null;
    }

    GameTagTypeImpl(@NotNull String name,
                    @NotNull Registry.Resource resource,
                    @NotNull Function<@NotNull String, @NotNull T> fromName,
                    @NotNull IntFunction<@NotNull T> fromId) {
        this(NamespaceID.from("minecraft", name), resource, fromName, fromId);
    }
}
