package net.minestom.server.tags;

import net.minestom.server.entity.EntityType;
import net.minestom.server.fluid.Fluid;
import net.minestom.server.gameevent.GameEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;

import org.jetbrains.annotations.Nullable;

/**
 * A type of {@link GameTag tag}.
 *
 * @param <T> the type
 */
public sealed interface GameTagType<T extends ProtocolObject> permits GameTagTypeImpl {

    @NotNull GameTagType<Block> BLOCKS = GameTagTypeImpl.BLOCKS;
    @NotNull GameTagType<Material> ITEMS = GameTagTypeImpl.ITEMS;
    @NotNull GameTagType<Fluid> FLUIDS = GameTagTypeImpl.FLUIDS;
    @NotNull GameTagType<EntityType> ENTITY_TYPES = GameTagTypeImpl.ENTITY_TYPES;
    @NotNull GameTagType<GameEvent> GAME_EVENTS = GameTagTypeImpl.GAME_EVENTS;

    static @NotNull Set<GameTagType<?>> values() {
        return GameTagTypeImpl.VALUES;
    }

    static @Nullable GameTagType<?> fromIdentifier(final @NotNull String identifier) {
        return GameTagTypeImpl.fromIdentifier(identifier);
    }

    @NotNull NamespaceID identifier();

    @NotNull Registry.Resource resource();

    @NotNull Function<@NotNull String, @NotNull T> fromName();

    @NotNull IntFunction<@NotNull T> fromId();
}
