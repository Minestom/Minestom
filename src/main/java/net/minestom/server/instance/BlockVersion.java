package net.minestom.server.instance;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public sealed interface BlockVersion permits BlockVersionImpl {
    boolean global();

    boolean compatible(BlockVersion other);
}
