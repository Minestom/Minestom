package net.minestom.server.entity.pathfinding.followers;

import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;

public interface NodeFollower {
    PhysicsResult moveTowards(@NotNull Point direction, double speed, Point lookAt);
    void jump();
}
