package net.minestom.server.entity.pathfinding;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an entity which can use the pathfinder.
 * <p>
 * All pathfinder methods are available with {@link #getNavigator()}.
 */
public interface NavigableEntity {
    @NotNull Navigator getNavigator();
}
