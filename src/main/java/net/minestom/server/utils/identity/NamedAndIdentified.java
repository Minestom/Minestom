package net.minestom.server.utils.identity;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * An object with a string name and a {@link UUID} identity.
 */
public interface NamedAndIdentified extends Named<String>, Identified {

    /**
     * Creates a {@link NamedAndIdentified} instance with an empty name and a random UUID.
     *
     * @return the named and identified instance
     */
    static @NotNull NamedAndIdentified empty() {
        return of("", UUID.randomUUID());
    }

    /**
     * Creates a {@link NamedAndIdentified} instance with a given name and a random UUID.
     *
     * @param name the name
     * @return the named and identified instance
     */
    static @NotNull NamedAndIdentified named(@NotNull String name) {
        return of(name, UUID.randomUUID());
    }

    /**
     * Creates a {@link NamedAndIdentified} instance with an empty name and a given UUID.
     *
     * @param uuid the uuid
     * @return the named and identified instance
     */
    static @NotNull NamedAndIdentified identified(@NotNull UUID uuid) {
        return of("", uuid);
    }

    /**
     * Creates a {@link NamedAndIdentified} instance with a given name and UUID.
     *
     * @param name the name
     * @param uuid the uuid
     * @return the named and identified instance
     */
    static @NotNull NamedAndIdentified of(@NotNull String name, @NotNull UUID uuid) {
        return new NamedAndIdentifiedImpl(name, uuid);
    }
}
