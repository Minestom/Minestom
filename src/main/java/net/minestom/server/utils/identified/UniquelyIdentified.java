package net.minestom.server.utils.identified;

import java.util.UUID;

import org.jetbrains.annotations.NotNull;

/**
 * An object that is uniquely identified by a UUID.
 * @see UUID
 */
public interface UniquelyIdentified {

    /**
     * Creates a new uniquely identified object from a random uuid.
     *
     * @return the uuid
     */
    static @NotNull UniquelyIdentified random() {
        return new UniquelyIdentifiedImpl(UUID.randomUUID());
    }

    /**
     * Creates a new uniquely identified object given a UUID.
     *
     * @param uuid the uuid
     * @return the uniquely identified instance
     */
    static @NotNull UniquelyIdentified of(@NotNull UUID uuid) {
        return new UniquelyIdentifiedImpl(uuid);
    }

    /**
     * Gets the unique identity for this object,
     *
     * @return the uuid
     */
    @NotNull UUID uniqueIdentity();
}
