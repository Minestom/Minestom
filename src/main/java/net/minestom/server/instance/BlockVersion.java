package net.minestom.server.instance;

import org.jetbrains.annotations.ApiStatus;

/**
 * Opaque type representing a version of a block area inside an {@link Instance}.
 * <p>
 * The version can also be global, meaning it represents the entire instance including unloaded areas.
 */
@ApiStatus.Experimental
public sealed interface BlockVersion permits BlockVersionImpl {
    boolean global();

    /**
     * Checks if this version is compatible with another version.
     *
     * @param other the other version to check compatibility with.
     * @return true if this version is compatible with the other version, false otherwise.
     */
    boolean compatible(BlockVersion other);
}
