package net.minestom.server.gamedata.tags;

import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
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
public final class Tag {
    private final NamespaceID name;
    private final Set<NamespaceID> values;

    /**
     * Creates a new empty tag. This does not cache the tag.
     */
    public Tag(NamespaceID name) {
        this.name = name;
        this.values = new HashSet<>();
    }

    /**
     * Creates a new tag with the given values. This does not cache the tag.
     */
    public Tag(NamespaceID name, Set<NamespaceID> values) {
        this.name = name;
        this.values = new HashSet<>(values);
    }

    /**
     * Checks whether the given id in inside this tag.
     *
     * @param id the id to check against
     * @return 'true' iif this tag contains the given id
     */
    public boolean contains(NamespaceID id) {
        return values.contains(id);
    }

    /**
     * Returns an immutable set of values present in this tag
     *
     * @return immutable set of values present in this tag
     */
    public Set<NamespaceID> getValues() {
        return Collections.unmodifiableSet(values);
    }

    /**
     * Returns the name of this tag
     */
    public NamespaceID getName() {
        return name;
    }

    public enum BasicType {
        BLOCKS("minecraft:block", Registry.Resource.BLOCK_TAGS,
                name -> Objects.requireNonNull(Block.fromNamespaceId(name)).id()),
        ITEMS("minecraft:item", Registry.Resource.ITEM_TAGS,
                name -> Objects.requireNonNull(Material.fromNamespaceId(name)).id()),
        FLUIDS("minecraft:fluid", Registry.Resource.FLUID_TAGS,
                name -> Registries.getFluid(name).ordinal()),
        ENTITY_TYPES("minecraft:entity_type", Registry.Resource.ENTITY_TYPE_TAGS,
                name -> Objects.requireNonNull(EntityType.fromNamespaceId(name)).id()),
        GAME_EVENTS("minecraft:game_event", Registry.Resource.GAMEPLAY_TAGS,
                name -> Registries.getFluid(name).ordinal());

        private final static BasicType[] VALUES = values();
        private final String identifier;
        private final Registry.Resource resource;
        private final Function<String, Integer> function;

        BasicType(@NotNull String identifier,
                  @NotNull Registry.Resource resource,
                  @NotNull Function<String, Integer> function) {
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
