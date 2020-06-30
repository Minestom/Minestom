package net.minestom.server.gamedata.tags;

import net.minestom.server.utils.NamespaceID;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a group of items, blocks, fluids, entity types or function.
 * Immutable by design
 */
public class Tag {

    public static final Tag EMPTY = new Tag(NamespaceID.from("minestom:empty"));
    private final NamespaceID name;

    private Set<NamespaceID> values;

    /**
     * Creates a new empty tag
     */
    public Tag(NamespaceID name) {
        this.name = name;
        values = new HashSet<>();
        lockValues();
    }

    /**
     * Creates a new tag with the contents of the container
     * @param manager Used to load tag contents (as tags are valid values inside 'values')
     * @param lowerPriority Tag contents from lower priority data packs. If 'replace' is false in 'container',
     *                      appends the contents of that pack to the one being constructed
     * @param container
     */
    public Tag(TagManager manager, NamespaceID name, String type, Tag lowerPriority, TagContainer container) throws FileNotFoundException {
        this.name = name;
        values = new HashSet<>();
        if(!container.replace) {
            values.addAll(lowerPriority.values);
        }
        Objects.requireNonNull(container.values, "Attempted to load from a TagContainer with no 'values' array");
        for(String line : container.values) {
            if(line.startsWith("#")) { // pull contents from a tag
                Tag subtag = manager.load(NamespaceID.from(line.substring(1)), type);
                values.addAll(subtag.values);
            } else {
                values.add(NamespaceID.from(line));
            }
        }

        lockValues();
    }

    private void lockValues() {
        values = Set.copyOf(values);
    }

    /**
     * Checks whether the given id in inside this tag
     * @param id the id to check against
     * @return 'true' iif this tag contains the given id
     */
    public boolean contains(NamespaceID id) {
        return values.contains(id);
    }

    /**
     * Returns an immutable set of values present in this tag
     * @return immutable set of values present in this tag
     */
    public Set<NamespaceID> getValues() {
        return values;
    }

    /**
     * Returns the name of this tag
     * @return
     */
    public NamespaceID getName() {
        return name;
    }

    public enum BasicTypes {
        BLOCKS,
        ITEMS,
        FLUIDS,
        ENTITY_TYPES
    }
}
