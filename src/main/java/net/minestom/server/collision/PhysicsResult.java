package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;

public record PhysicsResult(Pos newPosition, Vec newVelocity, boolean isOnGround,
                            boolean collisionX, boolean collisionY, boolean collisionZ,
                            Vec originalDelta, Point collidedBlockY, Block blockTypeY) {
}
