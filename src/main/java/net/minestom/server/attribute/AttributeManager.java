package net.minestom.server.attribute;

import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class AttributeManager {

    private final List<Attribute> attributes = new ArrayList<>();
    private final Map<NamespaceID, Attribute> attributesByName = new ConcurrentHashMap<>();
    private final Map<NamespaceID, Integer> idMappings = new ConcurrentHashMap<>();


    /**
     * Adds a new Attribute. This does NOT send the new list to players.
     *
     * @param attribute the attribute to add
     */
    public synchronized void register(Attribute attribute) {
        this.attributesByName.put(attribute.namespace(), attribute);
        this.idMappings.put(attribute.namespace(), attribute.registry().id());
        this.attributes.add(attribute);
    }

    public void loadVanillaAttributes() {
        for (Attribute attribute : AttributeImpl.values()) {
            if (getByName(attribute.namespace()) == null)
                register(attribute);
        }
    }

    /**
     * Removes a attribute. This does NOT send the new list to players.
     *
     * @param attribute the attribute to remove
     */
    public void removeBiome(@NotNull Attribute attribute) {
        var id = idMappings.get(attribute.namespace());
        if (id != null) {
            attributes.remove(id);
            attributesByName.remove(attribute.namespace());
            idMappings.remove(attribute.namespace());
        }
    }

    /**
     * Returns an immutable copy of the attribute already registered.
     *
     * @return an immutable copy of the attributes already registered
     */
    public synchronized Collection<Attribute> unmodifiableCollection() {
        return Collections.unmodifiableCollection(attributes);
    }

    /**
     * Gets a attribute by its id.
     *
     * @param id the id of the attribute
     * @return the {@link Attribute} linked to this id
     */
    @Nullable
    public synchronized Attribute getById(int id) {
        return attributes.get(id);
    }

    @Nullable
    public Attribute getByName(@NotNull NamespaceID namespaceID) {
        return attributesByName.get(namespaceID);
    }

    @Nullable
    public Attribute getByName(@NotNull String namespaceID) {
        NamespaceID namespace = NamespaceID.from(namespaceID);
        return getByName(namespace);
    }

    /**
     * Gets the id of a attribute.
     *`
     * @param attribute
     * @return the id of the attribute, or -1 if the attribute is not registered
     */
    public int getId(Attribute attribute) {
        return idMappings.getOrDefault(attribute.namespace(), -1);
    }
}
