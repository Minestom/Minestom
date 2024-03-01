package net.minestom.server.entity.pathfinding.followers;

public interface NodeFollower {
    /**
     * @return true if pathfinding should start on the ground
     */
    boolean requiresGroundStart();
}
