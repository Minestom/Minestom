package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;

public record PhysicsResultZero(Pos newPosition) implements PhysicsResult {
    
    @Override
    public Vec newVelocity() {
        return Vec.ZERO;
    }

    @Override
    public boolean isOnGround() {
        return false;
    }

    @Override
    public boolean collisionX() {
        return false;
    }

    @Override
    public boolean collisionY() {
        return false;
    }

    @Override
    public boolean collisionZ() {
        return false;
    }

    @Override
    public Vec originalDelta() {
        return null;
    }

    @Override
    public Point[] collisionPoints() {
        return new Point[3];
    }

    @Override
    public Shape[] collisionShapes() {
        return new Shape[3];
    }

    @Override
    public Point[] collisionShapePositions() {
        return new Point[3];
    }

    @Override
    public boolean hasCollision() {
        return false;
    }

    @Override
    public SweepResult sweepResult() {
        return SweepResult.NO_COLLISION;
    }
}
