package net.minestom.server.utils.identity;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static net.minestom.server.utils.identity.NamedAndIdentifiedImpl.EMPTY;

/**
 * An object with a {@link Component} name and a {@link UUID} identity.
 */
public interface NamedAndIdentified extends Named<Component>, Identified {

    /**
     * Creates a {@link NamedAndIdentified} instance with an empty name and a random UUID.
     *
     * @return the named and identified instance
     */
    static @NotNull NamedAndIdentified empty() {
        return of(EMPTY, UUID.randomUUID());
    }

    /**
     * Creates a {@link NamedAndIdentified} instance with a given name and a random UUID.
     *
     * @param name the name
     * @return the named and identified instance
     */
    static @NotNull NamedAndIdentified named(@NotNull Component name) {
        return of(name, UUID.randomUUID());
    }

    /**
     * Creates a {@link NamedAndIdentified} instance with an empty name and a given UUID.
     *
     * @param uuid the uuid
     * @return the named and identified instance
     */
    static @NotNull NamedAndIdentified identified(@NotNull UUID uuid) {
        return of(EMPTY, uuid);
    }

    /**
     * Creates a {@link NamedAndIdentified} instance with a given name and UUID.
     *
     * @param name the name
     * @param uuid the uuid
     * @return the named and identified instance
     */
    static @NotNull NamedAndIdentified of(@NotNull Component name, @NotNull UUID uuid) {
        return new NamedAndIdentifiedImpl(name, uuid);
    }
}
