package net.minestom.server.entity.pathfinding.followers;

public class GroundNodeFollower implements NodeFollower {
    @Override
    public boolean requiresGroundStart() {
        return true;
    }
}
