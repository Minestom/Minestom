package net.minestom.server.collision;

import it.unimi.dsi.fastutil.objects.Object2DoubleFunction;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.WorldBorder;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockGetter;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@ApiStatus.Internal
public final class CollisionUtils {
    private final static double EPSILON = 0.001;
    private static final CoordinateUpdater X_UPDATER = new CoordinateUpdater(key -> ((Vec) key).x(), key -> ((Vec) key).blockX(), (vec, value) -> vec.add(value, 0, 0));
    private static final CoordinateUpdater Y_UPDATER = new CoordinateUpdater(key -> ((Vec) key).y(), key -> ((Vec) key).blockY(), (vec, value) -> vec.add(0, value, 0));
    private static final CoordinateUpdater Z_UPDATER = new CoordinateUpdater(key -> ((Vec) key).z(), key -> ((Vec) key).blockZ(), (vec, value) -> vec.add(0, 0, value));

    /**
     * Moves an entity with physics applied (ie checking against blocks)
     *
     * @param entity the entity to move
     * @return the result of physics simulation
     */
    public static PhysicsResult handlePhysics(@NotNull Entity entity, @NotNull Vec deltaPosition) {
        final Instance instance = entity.getInstance();
        final Chunk originChunk = entity.getChunk();
        final Pos currentPosition = entity.getPosition();
        final BoundingBox boundingBox = entity.getBoundingBox();

        boolean xCheck = false, yCheck = false, zCheck = false;
        double xDelta = 0, yDelta = 0, zDelta = 0;

        if (deltaPosition.x() != 0) {
            final StepResult xCollision = stepAxis(instance, originChunk,
                    X_UPDATER, deltaPosition.x(),
                    deltaPosition.x() > 0 ? boundingBox.getRightFace() : boundingBox.getLeftFace());
            xCheck = xCollision.foundCollision();
            xDelta = xCollision.delta();
        }

        if (deltaPosition.y() != 0) {
            final StepResult yCollision = stepAxis(instance, originChunk,
                    Y_UPDATER, deltaPosition.y(),
                    deltaPosition.y() > 0 ? boundingBox.getTopFace() : boundingBox.getBottomFace());
            yCheck = yCollision.foundCollision();
            yDelta = yCollision.delta();
        }

        if (deltaPosition.z() != 0) {
            final StepResult zCollision = stepAxis(instance, originChunk,
                    Z_UPDATER, deltaPosition.z(),
                    deltaPosition.z() > 0 ? boundingBox.getBackFace() : boundingBox.getFrontFace());
            zCheck = zCollision.foundCollision();
            zDelta = zCollision.delta();
        }

        final Pos newPosition = xDelta == 0 && yDelta == 0 && zDelta == 0 ? currentPosition :
                currentPosition.add(xDelta, yDelta, zDelta);
        final Vec newVelocity = xCheck && yCheck && zCheck ? Vec.ZERO :
                new Vec(xCheck ? 0 : deltaPosition.x(),
                        yCheck ? 0 : deltaPosition.y(),
                        zCheck ? 0 : deltaPosition.z());
        return new PhysicsResult(newPosition, newVelocity, yCheck);
    }

    private static StepResult stepAxis(Instance instance, Chunk originChunk,
                                       CoordinateUpdater updater, double step,
                                       List<Vec> faces) {
        final double sign = Math.signum(step);

        double delta = 0;
        // Handle step being higher than 1
        {
            final double reducer = 1 * sign;
            while (Math.abs(step) > 1) {
                final StepResult stepResult = stepAxis(instance, originChunk, updater, reducer, faces);
                delta += stepResult.delta();
                if (stepResult.foundCollision()) return new StepResult(delta, true);
                step -= reducer;
            }
        }

        boolean collision = false;
        double displacement = 1;
        for (Vec face : faces) {
            final Vec newCorner = updater.adder().update(face, step);
            final Chunk chunk = ChunkUtils.retrieve(instance, originChunk, newCorner);
            if (chunk == null) continue;
            final Block block = chunk.getBlock(newCorner, BlockGetter.Condition.TYPE);
            // TODO support custom collision
            if (block == null || !block.isSolid()) continue;
            collision = true;
            final double newCoordinate = updater.blockFunction().getDouble(newCorner) + (sign > 0 ? 0 : 1);
            displacement = MathUtils.square(updater.fieldFunction().getDouble(face) - newCoordinate);
            break;
        }
        delta += step * displacement;
        if (Math.abs(delta) < EPSILON) return new StepResult(0, collision);
        return new StepResult(delta, collision);
    }

    /**
     * Applies world border collision.
     *
     * @param instance        the instance where the world border is
     * @param currentPosition the current position
     * @param newPosition     the future target position
     * @return the position with the world border collision applied (can be {@code newPosition} if not changed)
     */
    public static @NotNull Pos applyWorldBorder(@NotNull Instance instance,
                                                @NotNull Pos currentPosition, @NotNull Pos newPosition) {
        final WorldBorder worldBorder = instance.getWorldBorder();
        final WorldBorder.CollisionAxis collisionAxis = worldBorder.getCollisionAxis(newPosition);
        return switch (collisionAxis) {
            case NONE ->
                    // Apply velocity + gravity
                    newPosition;
            case BOTH ->
                    // Apply Y velocity/gravity
                    new Pos(currentPosition.x(), newPosition.y(), currentPosition.z());
            case X ->
                    // Apply Y/Z velocity/gravity
                    new Pos(currentPosition.x(), newPosition.y(), newPosition.z());
            case Z ->
                    // Apply X/Y velocity/gravity
                    new Pos(newPosition.x(), newPosition.y(), currentPosition.z());
        };
    }

    public record PhysicsResult(Pos newPosition, Vec newVelocity, boolean isOnGround) {
    }

    private record StepResult(double delta, boolean foundCollision) {
    }

    record CoordinateUpdater(Object2DoubleFunction<Vec> fieldFunction,
                             Object2DoubleFunction<Vec> blockFunction,
                             Adder adder) {
    }

    private interface Adder {
        Vec update(Vec vec, double value);
    }
}
