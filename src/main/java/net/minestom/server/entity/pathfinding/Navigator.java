package net.minestom.server.entity.pathfinding;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.pathfinding.followers.GroundNodeFollower;
import net.minestom.server.entity.pathfinding.followers.NodeFollower;
import net.minestom.server.entity.pathfinding.generators.NodeGenerator;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.WorldBorder;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

public final class Navigator {
    private static final int STUCK_INTERVAL = 40;
    private static final double MIN_PROGRESS = 0.15;

    private Point goal;
    private final Entity entity;

    private PPath computingPath;
    private PPath path;

    private double minDistance;
    private final EnumMap<PathType, Float> malus = new EnumMap<>(PathType.class);
    private double maxStepHeight = 1.0;
    private boolean canFloat;
    private int maxFallDistance = 5;

    NodeGenerator nodeGenerator = new net.minestom.server.entity.pathfinding.generators.GroundNodeGenerator();
    private NodeFollower nodeFollower;
    private Pos lastProgress;
    private int stuckTicks;
    private Point lastNode;
    private int nodeTicks;
    private int expectedNodeTicks;

    public Navigator(Entity entity) {
        this.entity = entity;
        nodeFollower = new GroundNodeFollower(entity);
    }

    public PPath.State getState() {
        if (path == null && computingPath == null) return PPath.State.INVALID;
        if (path == null) return computingPath.getState();
        return path.getState();
    }

    public synchronized boolean setPathTo(@Nullable Point point) {
        BoundingBox bb = this.entity.getBoundingBox();
        double centerToCorner = Math.sqrt(bb.width() * bb.width() + bb.depth() * bb.depth()) / 2;
        return setPathTo(point, centerToCorner, null);
    }

    public synchronized boolean setPathTo(@Nullable Point point, double minDistance, @Nullable Runnable onComplete) {
        return setPathTo(point, minDistance, 50, 20, onComplete);
    }

    public synchronized boolean setPathTo(@Nullable Point point, double minDistance, double maxDistance, double pathVariance, @Nullable Runnable onComplete) {
        final Instance instance = entity.getInstance();
        if (point == null) {
            this.path = null;
            reset();
            return false;
        }

        if (instance == null) {
            this.path = null;
            return false;
        }

        final WorldBorder worldBorder = instance.getWorldBorder();
        if (!worldBorder.inBounds(point)) {
            return false;
        }

        final Chunk chunk = instance.getChunkAt(point);
        if (!ChunkUtils.isLoaded(chunk)) {
            return false;
        }

        this.minDistance = minDistance;
        resetTracking();
        if (this.entity.getPosition().distance(point) < minDistance) {
            if (onComplete != null) onComplete.run();
            return false;
        }

        if (point.sameBlock(entity.getPosition())) {
            if (onComplete != null) onComplete.run();
            return false;
        }

        if (this.computingPath != null) this.computingPath.setState(PPath.State.TERMINATING);

        configureGenerator();
        var async = AsyncPathGenerator.generateAsync(instance,
                this.entity.getPosition(),
                point,
                minDistance, maxDistance,
                pathVariance,
                this.entity.getBoundingBox(),
                this.entity.isOnGround(),
                this.nodeGenerator,
                onComplete);
        async.thenAccept(pathResult -> {
            synchronized (Navigator.this) {
                if (pathResult.getState() == PPath.State.INVALID) return;
                computingPath = pathResult;
            }
        });

        this.goal = point;
        return true;
    }

    @ApiStatus.Internal
    public synchronized void tick() {
        if (goal == null) return;
        if (entity instanceof LivingEntity living && living.isDead()) return;

        if (computingPath != null && (computingPath.getState() == PPath.State.COMPUTED || computingPath.getState() == PPath.State.BEST_EFFORT)) {
            path = computingPath;
            computingPath = null;
            resetTracking();
        }

        if (path == null) return;

        if (path.getState() == PPath.State.COMPUTED || path.getState() == PPath.State.BEST_EFFORT) {
            path.setState(PPath.State.FOLLOWING);
            for (int i = 0; i < path.getNodes().size(); i++) {
                if (isSameBlock(path.getNodes().get(i), entity.getPosition())) {
                    path.getNodes().subList(0, i).clear();
                    break;
                }
            }
            lastNode = null;
            nodeTicks = 0;
            expectedNodeTicks = 0;
        }

        if (path.getState() != PPath.State.FOLLOWING) return;

        trackStuck();

        if (this.entity.getPosition().distance(goal) < minDistance) {
            path.runComplete();
            path = null;
            resetTracking();
            return;
        }

        Point current = path.getCurrent();
        Point next = path.getNext();

        if (current == null || path.getCurrentType() == PNode.Type.REPATH || path.getCurrentType() == null) {
            if (computingPath != null && computingPath.getState() == PPath.State.CALCULATING) return;

            computingPath = PathGenerator.generate(entity.getInstance(),
                    entity.getPosition(),
                    goal.asPos(),
                    minDistance, path.maxDistance(),
                    path.pathVariance(), entity.getBoundingBox(), this.entity.isOnGround(), nodeGenerator, null);

            return;
        }

        if (next == null) {
            path.setState(PPath.State.INVALID);
            return;
        }

        final double tolerance = Math.max(0.6, entity.getBoundingBox().width() * 0.75);
        final double distToCurrent = entity.getPosition().distance(current);
        final double baseSpeed = nodeFollower.movementSpeed();
        final double slowRadius = 1.5;
        final double scale = Math.min(1.0, distToCurrent / slowRadius);
        final double speed = Math.max(0.02, baseSpeed * scale);

        updateNodeTiming(current, distToCurrent, speed);

        if (!next.equals(current)) {
            double distNext = entity.getPosition().distance(next);
            if (distNext + tolerance * 0.5 < distToCurrent) {
                path.next();
                lastNode = null;
                nodeTicks = 0;
                expectedNodeTicks = 0;
                return;
            }
        }

        boolean nextIsRepath = next.sameBlock(Pos.ZERO);

        // Jump only when height difference exceeds step-up capability
        double heightDiff = current.y() - entity.getPosition().y();
        if (path.getCurrentType() == PNode.Type.JUMP && heightDiff > maxStepHeight) {
            double horizDist = Math.sqrt(
                Math.pow(current.x() - entity.getPosition().x(), 2) +
                Math.pow(current.z() - entity.getPosition().z(), 2)
            );
            if (horizDist < 1.5 && entity.isOnGround()) {
                nodeFollower.jump(current, next);
            }
        }

        nodeFollower.moveTowards(current, speed, nextIsRepath ? current : next);

        if (distToCurrent <= tolerance) {
            path.next();
            lastNode = null;
            nodeTicks = 0;
            expectedNodeTicks = 0;
        } else if (nodeTicks > expectedNodeTicks * 3 && (computingPath == null || computingPath.getState() != PPath.State.CALCULATING)) {
            computingPath = PathGenerator.generate(entity.getInstance(),
                    entity.getPosition(),
                    goal.asPos(),
                    minDistance, path.maxDistance(),
                    path.pathVariance(), entity.getBoundingBox(), this.entity.isOnGround(), nodeGenerator, null);
            nodeTicks = 0;
            lastNode = null;
        }
    }

    public @Nullable Point getGoalPosition() {
        return goal;
    }

    public Entity getEntity() {
        return entity;
    }

    public void reset() {
        if (this.path != null) this.path.setState(PPath.State.TERMINATING);
        this.goal = null;
        this.path = null;
        resetTracking();

        if (this.computingPath != null) this.computingPath.setState(PPath.State.TERMINATING);
        this.computingPath = null;
    }

    public boolean isComplete() {
        if (this.path == null) return true;
        return goal == null || entity.getPosition().sameBlock(goal);
    }

    public List<PNode> getNodes() {
        if (this.path == null && computingPath == null) return null;
        if (this.path == null) return computingPath.getNodes();
        return this.path.getNodes();
    }

    public Point getPathPosition() {
        return goal;
    }

    public void setNodeFollower(Supplier<NodeFollower> nodeFollower) {
        this.nodeFollower = nodeFollower.get();
    }

    public void setNodeGenerator(Supplier<NodeGenerator> nodeGenerator) {
        this.nodeGenerator = nodeGenerator.get();
        configureGenerator();
    }

    public void setPathfindingMalus(PathType type, float value) {
        this.malus.put(type, value);
        configureGenerator();
    }

    public float getPathfindingMalus(PathType type) {
        return this.malus.getOrDefault(type, type.getMalus());
    }

    public void setMaxStepHeight(double stepHeight) {
        this.maxStepHeight = Math.max(0.0, stepHeight);
        configureGenerator();
    }

    public double getMaxStepHeight() {
        return maxStepHeight;
    }

    public void setCanFloat(boolean canFloat) {
        this.canFloat = canFloat;
        configureGenerator();
    }

    public boolean canFloat() {
        return canFloat;
    }

    public void setMaxFallDistance(int maxFallDistance) {
        this.maxFallDistance = Math.max(0, maxFallDistance);
        configureGenerator();
    }

    public int getMaxFallDistance() {
        return maxFallDistance;
    }

    private void resetTracking() {
        stuckTicks = 0;
        lastProgress = null;
        lastNode = null;
        nodeTicks = 0;
        expectedNodeTicks = 0;
    }

    private void trackStuck() {
        if (path == null || path.getState() != PPath.State.FOLLOWING) {
            resetTracking();
            return;
        }
        if (goal == null || entity.getInstance() == null) return;

        final Pos pos = entity.getPosition();
        if (lastProgress == null) {
            lastProgress = pos;
            return;
        }

        final double minProg = Math.max(MIN_PROGRESS, entity.getBoundingBox().width() * 0.25);
        if (pos.distanceSquared(lastProgress) > minProg * minProg) {
            lastProgress = pos;
            stuckTicks = 0;
            return;
        }

        stuckTicks++;
        if (stuckTicks < STUCK_INTERVAL) return;
        if (computingPath != null && computingPath.getState() == PPath.State.CALCULATING) return;

        computingPath = PathGenerator.generate(
                entity.getInstance(),
                entity.getPosition(),
                goal.asPos(),
                minDistance,
                path.maxDistance(),
                path.pathVariance(),
                entity.getBoundingBox(),
                this.entity.isOnGround(),
                nodeGenerator,
                null
        );
        stuckTicks = 0;
        lastProgress = pos;
    }

    private void updateNodeTiming(Point current, double dist, double speed) {
        if (lastNode == null || !current.equals(lastNode)) {
            lastNode = current;
            nodeTicks = 0;
            double expected = dist / Math.max(speed, 0.05);
            expectedNodeTicks = (int) Math.max(5, Math.ceil(expected * 2));
            return;
        }
        nodeTicks++;
    }

    private void configureGenerator() {
        if (nodeGenerator != null) {
            nodeGenerator.setPathMalusProvider(this::getPathfindingMalus);
            nodeGenerator.setMaxUpStep(maxStepHeight);
            nodeGenerator.setCanFloat(canFloat);
            nodeGenerator.setMaxFallDistance(maxFallDistance);
        }

        if (nodeFollower instanceof GroundNodeFollower groundFollower) {
            groundFollower.setMaxStepHeight(maxStepHeight);
        }
    }

    private static boolean isSameBlock(PNode pNode, Pos position) {
        return Math.floor(pNode.x()) == position.blockX() && Math.floor(pNode.y()) == position.blockY() && Math.floor(pNode.z()) == position.blockZ();
    }
}
