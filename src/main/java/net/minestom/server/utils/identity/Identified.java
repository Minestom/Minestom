package net.minestom.server.utils.identity;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * An object with a unique identifier.
 */
@FunctionalInterface
public interface Identified {

    /**
     * Gets the unique identifier for this object.
     *
     * @return the uuid
     */
    @NotNull UUID getUuid();
}
