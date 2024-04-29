package net.minestom.server.entity.pathfinding;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.pathfinding.followers.GroundNodeFollower;
import net.minestom.server.entity.pathfinding.followers.NodeFollower;
import net.minestom.server.entity.pathfinding.generators.GroundNodeGenerator;
import net.minestom.server.entity.pathfinding.generators.NodeGenerator;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.WorldBorder;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * Necessary object for all {@link NavigableEntity}.
 */
public final class Navigator {
    private Point goalPosition;
    private final Entity entity;

    // Essentially a double buffer. Wait until a path is done computing before replacing the old one.
    private PPath computingPath;
    private PPath path;

    private double minimumDistance;

     NodeGenerator nodeGenerator = new GroundNodeGenerator();
    private NodeFollower nodeFollower;

    public Navigator(@NotNull Entity entity) {
        this.entity = entity;
        nodeFollower = new GroundNodeFollower(entity);
    }

    public @NotNull PPath.PathState getState() {
        if (path == null && computingPath == null) return PPath.PathState.INVALID;
        if (path == null) return computingPath.getState();
        return path.getState();
    }

    public synchronized boolean setPathTo(@Nullable Point point) {
        BoundingBox bb = this.entity.getBoundingBox();
        double centerToCorner = Math.sqrt(bb.width() * bb.width() + bb.depth() * bb.depth()) / 2;
        return setPathTo(point, centerToCorner, null);
    }

    public synchronized boolean setPathTo(@Nullable Point point, double minimumDistance, @Nullable Runnable onComplete) {
        return setPathTo(point, minimumDistance, 50, 20, onComplete);
    }

    /**
     * Sets the path to {@code position} and ask the entity to follow the path.
     *
     * @param point the position to find the path to, null to reset the pathfinder
     * @param minimumDistance distance to target when completed
     * @param maxDistance maximum search distance
     * @param pathVariance how far to search off of the direct path. For open worlds, this can be low (around 20) and for large mazes this needs to be very high.
     * @param onComplete called when the path has been completed
     * @return true if a path is being generated
     */
    public synchronized boolean setPathTo(@Nullable Point point, double minimumDistance, double maxDistance, double pathVariance, @Nullable Runnable onComplete) {
        final Instance instance = entity.getInstance();
        if (point == null) {
            this.path = null;
            return false;
        }

        // Can't path with a null instance.
        if (instance == null) {
            this.path = null;
            return false;
        }

        // Can't path outside the world border
        final WorldBorder worldBorder = instance.getWorldBorder();
        if (!worldBorder.isInside(point)) {
            return false;
        }
        // Can't path in an unloaded chunk
        final Chunk chunk = instance.getChunkAt(point);
        if (!ChunkUtils.isLoaded(chunk)) {
            return false;
        }

        this.minimumDistance = minimumDistance;
        if (this.entity.getPosition().distance(point) < minimumDistance) {
            if (onComplete != null) onComplete.run();
            return false;
        }

        if (point.sameBlock(entity.getPosition())) {
            if (onComplete != null) onComplete.run();
            return false;
        }

        if (this.computingPath != null) this.computingPath.setState(PPath.PathState.TERMINATING);

        this.computingPath = PathGenerator.generate(instance,
                        this.entity.getPosition(),
                        point,
                        minimumDistance, maxDistance,
                        pathVariance,
                this.entity.getBoundingBox(),
                this.entity.isOnGround(),
                this.nodeGenerator,
                onComplete);

        this.goalPosition = point;
        return true;
    }

    @ApiStatus.Internal
    public synchronized void tick() {
        if (goalPosition == null) return; // No path
        if (entity instanceof LivingEntity && ((LivingEntity) entity).isDead()) return; // No pathfinding tick for dead entities
        if (computingPath != null && (computingPath.getState() == PPath.PathState.COMPUTED || computingPath.getState() == PPath.PathState.BEST_EFFORT)) {
            path = computingPath;
            computingPath = null;
        }

        if (path == null) return;

        // If the path is computed start following it
        if (path.getState() == PPath.PathState.COMPUTED || path.getState() == PPath.PathState.BEST_EFFORT) {
            path.setState(PPath.PathState.FOLLOWING);
            // Remove nodes that are too close to the start. Prevents doubling back to hit points that have already been hit
            for (int i = 0; i < path.getNodes().size(); i++) {
                if (isSameBlock(path.getNodes().get(i), entity.getPosition())) {
                    path.getNodes().subList(0, i).clear();
                    break;
                }
            }
        }

        // If the state is not following, wait until it is
        if (path.getState() != PPath.PathState.FOLLOWING) return;

        // If we're near the entity, we're done
        if (this.entity.getPosition().distance(goalPosition) < minimumDistance) {
            path.runComplete();
            path = null;

            return;
        }

        Point currentTarget = path.getCurrent();
        Point nextTarget = path.getNext();

        // If we're at the end of the path, navigate directly to the entity
        if (nextTarget == null) {
            path.setState(PPath.PathState.INVALID);
            return;
        }

        // Repath
        if (currentTarget == null || path.getCurrentType() == PNode.NodeType.REPATH || path.getCurrentType() == null) {
            if (computingPath != null && computingPath.getState() == PPath.PathState.CALCULATING) return;

            computingPath = PathGenerator.generate(entity.getInstance(),
                    entity.getPosition(),
                    Pos.fromPoint(goalPosition),
                    minimumDistance, path.maxDistance(),
                    path.pathVariance(), entity.getBoundingBox(), this.entity.isOnGround(), nodeGenerator, null);

            return;
        }

        boolean nextIsRepath = nextTarget.sameBlock(Pos.ZERO);
        nodeFollower.moveTowards(currentTarget, nodeFollower.movementSpeed(), nextIsRepath ? currentTarget : nextTarget);

        if (nodeFollower.isAtPoint(currentTarget)) path.next();
        else if (path.getCurrentType() == PNode.NodeType.JUMP) nodeFollower.jump(currentTarget, nextTarget);
    }

    /**
     * Gets the target pathfinder position.
     *
     * @return the target pathfinder position, null if there is no one
     */
    public @Nullable Point getGoalPosition() {
        return goalPosition;
    }

    /**
     * Gets the entity which is navigating.
     *
     * @return the entity
     */
    public @NotNull Entity getEntity() {
        return entity;
    }

    public void reset() {
        if (this.path != null) this.path.setState(PPath.PathState.TERMINATING);
        this.goalPosition = null;
        this.path = null;

        if (this.computingPath != null) this.computingPath.setState(PPath.PathState.TERMINATING);
        this.computingPath = null;
    }

    public boolean isComplete() {
        if (this.path == null) return true;
        return goalPosition == null || entity.getPosition().sameBlock(goalPosition);
    }

    public List<PNode> getNodes() {
        if (this.path == null && computingPath == null) return null;
        if (this.path == null) return computingPath.getNodes();
        return this.path.getNodes();
    }

    public Point getPathPosition() {
        return goalPosition;
    }

    public void setNodeFollower(@NotNull Supplier<NodeFollower> nodeFollower) {
        this.nodeFollower = nodeFollower.get();
    }

    public void setNodeGenerator(@NotNull Supplier<NodeGenerator> nodeGenerator) {
        this.nodeGenerator = nodeGenerator.get();
    }

    /**
     * Visualise path for debugging
     * @param path the path to draw
     */
    private void drawPath(PPath path) {
        if (path == null) return;

        for (PNode point : path.getNodes()) {
            var packet = new ParticlePacket(Particle.COMPOSTER, point.x(), point.y() + 0.5, point.z(), 0, 0, 0, 0, 1);
            entity.sendPacketToViewers(packet);
        }
    }

    private static boolean isSameBlock(PNode pNode, Pos position) {
        return Math.floor(pNode.x()) == position.blockX() && Math.floor(pNode.y()) == position.blockY() && Math.floor(pNode.z()) == position.blockZ();
    }
}
