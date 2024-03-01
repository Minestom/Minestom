package net.minestom.server.entity.pathfinding.followers;

public class FlyingNodeFollower implements NodeFollower {
    @Override
    public boolean requiresGroundStart() {
        return false;
    }
}
