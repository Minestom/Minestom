package net.minestom.server.utils.identity;

import net.kyori.adventure.text.Component;

import java.util.UUID;

/**
 * An object with a {@link Component} name and a {@link UUID} identity.
 */
public interface NamedAndIdentified {

    /**
     * Creates a {@link NamedAndIdentified} instance with an empty name and a random UUID.
     *
     * @return the named and identified instance
     */
    static NamedAndIdentified empty() {
        return of(Component.empty(), UUID.randomUUID());
    }

    /**
     * Creates a {@link NamedAndIdentified} instance with a given name and a random UUID.
     *
     * @param name the name
     * @return the named and identified instance
     */
    static NamedAndIdentified named(String name) {
        return of(name, UUID.randomUUID());
    }

    /**
     * Creates a {@link NamedAndIdentified} instance with a given name and a random UUID.
     *
     * @param name the name
     * @return the named and identified instance
     */
    static NamedAndIdentified named(Component name) {
        return of(name, UUID.randomUUID());
    }

    /**
     * Creates a {@link NamedAndIdentified} instance with an empty name and a given UUID.
     *
     * @param uuid the uuid
     * @return the named and identified instance
     */
    static NamedAndIdentified identified(UUID uuid) {
        return of(Component.empty(), uuid);
    }

    /**
     * Creates a {@link NamedAndIdentified} instance with a given name and UUID.
     *
     * @param name the name
     * @param uuid the uuid
     * @return the named and identified instance
     */
    static NamedAndIdentified of(String name, UUID uuid) {
        return new NamedAndIdentifiedImpl(name, uuid);
    }

    /**
     * Creates a {@link NamedAndIdentified} instance with a given name and UUID.
     *
     * @param name the name
     * @param uuid the uuid
     * @return the named and identified instance
     */
    static NamedAndIdentified of(Component name, UUID uuid) {
        return new NamedAndIdentifiedImpl(name, uuid);
    }

    /**
     * Gets the name of this object.
     *
     * @return the name
     */
    Component getName();

    /**
     * Gets the UUID of this object.
     *
     * @return the uuid
     */
    UUID getUuid();
}
