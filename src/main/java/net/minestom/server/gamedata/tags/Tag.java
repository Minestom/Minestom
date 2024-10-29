package net.minestom.server.gamedata.tags;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.FluidRegistries;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * Represents a group of items, blocks, fluids, entity types or function.
 * Immutable by design
 */
public final class Tag implements ProtocolObject, Keyed {
    private final NamespaceID name;
    private final Set<NamespaceID> values;

    /**
     * Creates a new empty tag. This does not cache the tag.
     */
    public Tag(@NotNull NamespaceID name) {
        this.name = name;
        this.values = new HashSet<>();
    }

    /**
     * Creates a new tag with the given values. This does not cache the tag.
     */
    public Tag(@NotNull NamespaceID name, @NotNull Set<NamespaceID> values) {
        this.name = name;
        this.values = new HashSet<>(values);
    }

    /**
     * Checks whether the given id in inside this tag.
     *
     * @param id the id to check against
     * @return 'true' iif this tag contains the given id
     */
    public boolean contains(@NotNull NamespaceID id) {
        return values.contains(id);
    }

    /**
     * Returns an immutable set of values present in this tag
     *
     * @return immutable set of values present in this tag
     */
    public @NotNull Set<NamespaceID> getValues() {
        return Collections.unmodifiableSet(values);
    }

    public @NotNull NamespaceID namespace() {
        return name;
    }

    @Contract(pure = true)
    public @NotNull String name() {
        return namespace().asString();
    }

    @Override
    @Contract(pure = true)
    public @NotNull Key key() {
        return namespace();
    }

    /**
     * Returns the name of this tag
     */
    @Deprecated
    public NamespaceID getName() {
        return name;
    }

    @Override
    public String toString() {
        return "#" + name.asString();
    }

    public enum BasicType {
        BLOCKS("minecraft:block", Registry.Resource.BLOCK_TAGS,
                name -> Objects.requireNonNull(Block.fromNamespaceId(name)).id()),
        ITEMS("minecraft:item", Registry.Resource.ITEM_TAGS,
                name -> Objects.requireNonNull(Material.fromNamespaceId(name)).id()),
        FLUIDS("minecraft:fluid", Registry.Resource.FLUID_TAGS,
                name -> FluidRegistries.getFluid(name).ordinal()),
        ENTITY_TYPES("minecraft:entity_type", Registry.Resource.ENTITY_TYPE_TAGS,
                name -> Objects.requireNonNull(EntityType.fromNamespaceId(name)).id()),
        GAME_EVENTS("minecraft:game_event", Registry.Resource.GAMEPLAY_TAGS,
                name -> FluidRegistries.getFluid(name).ordinal()),
        SOUND_EVENTS("minecraft:sound_event", null, null), // Seems not to be included in server data
        POTION_EFFECTS("minecraft:potion_effect", null, null), // Seems not to be included in server data

        //todo this is cursed. it does not update as the registry changes. Fix later.
        ENCHANTMENTS("minecraft:enchantment", Registry.Resource.ENCHANTMENT_TAGS,
                name -> MinecraftServer.getEnchantmentRegistry().getId(DynamicRegistry.Key.of(name))),
        BIOMES("minecraft:worldgen/biome", Registry.Resource.BIOME_TAGS,
                name -> MinecraftServer.getBiomeRegistry().getId(DynamicRegistry.Key.of(name)));

        private final static BasicType[] VALUES = values();
        private final String identifier;
        private final Registry.Resource resource;
        private final Function<String, Integer> function;

        BasicType(@NotNull String identifier,
                  @Nullable Registry.Resource resource,
                  @Nullable Function<String, Integer> function) {
            this.identifier = identifier;
            this.resource = resource;
            this.function = function;
        }

        public @NotNull String getIdentifier() {
            return identifier;
        }

        public Registry.Resource getResource() {
            return resource;
        }

        public Function<String, Integer> getFunction() {
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
