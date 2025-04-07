package net.minestom.server.gamedata.tags;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.entity.EntityType;
import net.minestom.server.game.GameEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.registry.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Represents a group of items, blocks, fluids, entity types or function.
 * Immutable by design
 */
public final class Tag implements ProtocolObject, Keyed {
    private final Key name;
    private final Set<Key> values;

    /**
     * Creates a new empty tag. This does not cache the tag.
     */
    public Tag(@NotNull Key name) {
        this.name = name;
        this.values = new HashSet<>();
    }

    /**
     * Creates a new tag with the given values. This does not cache the tag.
     */
    public Tag(@NotNull Key name, @NotNull Set<Key> values) {
        this.name = name;
        this.values = new HashSet<>(values);
    }

    /**
     * Checks whether the given id in inside this tag.
     *
     * @param id the id to check against
     * @return 'true' iif this tag contains the given id
     */
    public boolean contains(@NotNull Key id) {
        return values.contains(id);
    }

    /**
     * Returns an immutable set of values present in this tag
     *
     * @return immutable set of values present in this tag
     */
    public @NotNull Set<Key> getValues() {
        return Collections.unmodifiableSet(values);
    }

    @Contract(pure = true)
    public @NotNull String name() {
        return key().asString();
    }

    @Override
    @Contract(pure = true)
    public @NotNull Key key() {
        return name;
    }

    /**
     * Returns the name of this tag
     */
    @Deprecated
    public Key getName() {
        return name;
    }

    @Override
    public String toString() {
        return "#" + name.asString();
    }

    public enum BasicType {
        BLOCKS("minecraft:block", RegistryData.Resource.BLOCK_TAGS,
                (blockName, registries) -> Optional.ofNullable(Block.fromKey(blockName)).map(Block::id)),
        ITEMS("minecraft:item", RegistryData.Resource.ITEM_TAGS,
                (itemName, registries) -> Optional.ofNullable(Material.fromKey(itemName)).map(Material::id)),
        FLUIDS("minecraft:fluid", RegistryData.Resource.FLUID_TAGS,
                (name, registries) -> Optional.of(name).map(FluidRegistries::getFluid).map(Enum::ordinal)),
        ENTITY_TYPES("minecraft:entity_type", RegistryData.Resource.ENTITY_TYPE_TAGS,
                (entityName, registries) -> Optional.ofNullable(EntityType.fromKey(entityName)).map(EntityType::id)),
        GAME_EVENTS("minecraft:game_event", RegistryData.Resource.GAME_EVENT_TAGS,
                (eventName, registries) -> Optional.ofNullable(GameEvent.fromKey(eventName)).map(GameEvent::id)),
        SOUND_EVENTS("minecraft:sound_event", null, null), // Seems not to be included in server data
        POTION_EFFECTS("minecraft:potion_effect", null, null), // Seems not to be included in server data

        ENCHANTMENTS("minecraft:enchantment", RegistryData.Resource.ENCHANTMENT_TAGS,
                (name, registries) -> Optional.of(DynamicRegistry.Key.of(name))
                        .map(DynamicRegistry.Key::key)
                        .map(registries.enchantment()::getId)),
        BIOMES("minecraft:worldgen/biome", RegistryData.Resource.BIOME_TAGS,
                (name, registries) -> Optional.of(DynamicRegistry.Key.of(name))
                        .map(DynamicRegistry.Key::key)
                        .map(registries.biome()::getId)),
        INSTRUMENTS("minecraft:instrument", RegistryData.Resource.INSTRUMENT_TAGS,
                (name, registries) -> Optional.of(DynamicRegistry.Key.of(name))
                        .map(DynamicRegistry.Key::key)
                        .map(registries.instrument()::getId)),
        ;

        private static final BasicType[] VALUES = values();
        private final String identifier;
        private final RegistryData.Resource resource;
        private final BiFunction<String, Registries, Optional<Integer>> function;

        BasicType(@NotNull String identifier,
                  @Nullable RegistryData.Resource resource,
                  @Nullable BiFunction<String, Registries, Optional<Integer>> function) {
            this.identifier = identifier;
            this.resource = resource;
            this.function = function;
        }

        public @NotNull String getIdentifier() {
            return identifier;
        }

        public RegistryData.Resource getResource() {
            return resource;
        }

        public BiFunction<String, Registries, Optional<Integer>> getFunction() {
            return function;
        }

        public static @Nullable Tag.BasicType fromIdentifer(@NotNull String identifier) {
            for (BasicType value : VALUES) {
                if (value.identifier.equals(identifier)) {
                    return value;
                }
            }
            return null;
        }
    }
}
