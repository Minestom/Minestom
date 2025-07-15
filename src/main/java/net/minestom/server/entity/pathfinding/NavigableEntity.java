package net.minestom.server.entity.pathfinding;


/**
 * Represents an entity which can use the pathfinder.
 * <p>
 * All pathfinder methods are available with {@link #getNavigator()}.
 */
public interface NavigableEntity {
    Navigator getNavigator();
}
