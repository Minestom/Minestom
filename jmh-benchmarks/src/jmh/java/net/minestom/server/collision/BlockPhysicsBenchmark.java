package net.minestom.server.collision;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.WorldBorder;
import net.minestom.server.instance.block.Block;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * Benchmarks the block-physics hot path ({@link CollisionUtils#handlePhysics} /
 * {@link PhysicsUtils#simulateMovement}) which runs for every entity every tick.
 * <p>
 * Each benchmark method is crafted to exercise a distinct branch of the physics code:
 * <ul>
 *     <li>{@link #zeroVelocity()} - the {@code velocity.isZero()} early return</li>
 *     <li>{@link #cachedStanding()} - the {@code cachedPhysics} fast-exit (resting on ground)</li>
 *     <li>{@link #fallThroughAir()} - {@code fastPhysics}, no collision (face traversal only)</li>
 *     <li>{@link #fallIntoFloor()} - {@code fastPhysics}, vertical collision</li>
 *     <li>{@link #walkOnFloor()} - {@code fastPhysics}, horizontal move + gravity into floor</li>
 *     <li>{@link #diagonalMove()} - {@code fastPhysics} diagonal special-case</li>
 *     <li>{@link #largeMoveSlow()} - {@code slowPhysics} ray-cast (velocity length &gt; 1)</li>
 *     <li>{@link #fenceCollision()} - multi-box shape + tall-below ({@code shouldCheckLower}) path</li>
 *     <li>{@link #denseCollision()} - collisions on all axes, multiple step-physics iterations</li>
 *     <li>{@link #simulateOnGround()} - full movement incl. friction lookup + velocity update</li>
 *     <li>{@link #simulateFalling()} - full movement incl. gravity velocity update</li>
 * </ul>
 */
@BenchmarkMode({Mode.AverageTime, Mode.Throughput})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 8, time = 1)
@Fork(2)
public class BlockPhysicsBenchmark {

    // Player-sized bounding box (0.6 x 1.8 x 0.6)
    private static final BoundingBox PLAYER_BB = new BoundingBox(0.6, 1.8, 0.6);
    // Representative player aerodynamics (gravity, horizontal drag, vertical drag)
    private static final Aerodynamics AERO = new Aerodynamics(0.08, 0.91, 0.98);
    private static final WorldBorder BORDER = WorldBorder.DEFAULT_BORDER;

    // --- In-memory block getters ---

    /** Everything is air: exercises face traversal with no collisions. */
    private static final Block.Getter AIR_GETTER = condGetter((x, y, z) -> Block.AIR);

    /** Stone floor with its top surface at y=64 (block layer y<=63), air above. */
    private static final Block.Getter FLOOR_GETTER = condGetter((x, y, z) -> y <= 63 ? Block.STONE : Block.AIR);

    /** Solid stone everywhere: maximal collision on every axis. */
    private static final Block.Getter DENSE_GETTER = condGetter((x, y, z) -> Block.STONE);

    /**
     * Stone floor (y<=62) topped by a layer of fences at y=63. Fences are multi-box, 1.5 tall shapes,
     * so this drives {@link ShapeImpl#intersectBoxSwept} over several boxes and the tall-below branch.
     */
    private static final Block.Getter FENCE_GETTER = condGetter((x, y, z) -> {
        if (y <= 62) return Block.STONE;
        if (y == 63) return Block.OAK_FENCE;
        return Block.AIR;
    });

    // Getters that take a read lock per block lookup, mimicking the real ChunkCache cost (which the
    // plain lambda getters above do not capture). Used to measure the benefit of fewer/deduplicated
    // block lookups in the physics paths.
    private static final Block.Getter LOCKING_FLOOR_GETTER = lockingGetter((x, y, z) -> y <= 63 ? Block.STONE : Block.AIR);
    private static final Block.Getter LOCKING_DENSE_GETTER = lockingGetter((x, y, z) -> Block.STONE);
    private static final Block.Getter LOCKING_AIR_GETTER = lockingGetter((x, y, z) -> Block.AIR);

    // Cached "standing on the ground" physics result, primed in setup.
    private Pos restPos;
    private Vec restVelocity;
    private PhysicsResult cachedResult;

    @Setup
    public void setup() {
        // Prime the cached-physics fast path: rest an entity on the floor and feed the result
        // back until it stabilizes into a cacheable state.
        restPos = new Pos(0.5, 64.0, 0.5);
        restVelocity = new Vec(0, -AERO.gravity() * AERO.verticalAirResistance(), 0);
        PhysicsResult result = null;
        for (int i = 0; i < 8; i++) {
            result = CollisionUtils.handlePhysics(FLOOR_GETTER, PLAYER_BB, restPos, restVelocity, result, false);
            restPos = result.newPosition();
        }
        this.cachedResult = result;
        if (!cachedResult.collisionY()) {
            throw new IllegalStateException("Failed to prime resting state; collisionY expected to be true");
        }
    }

    // --- handlePhysics paths ---

    @Benchmark
    public PhysicsResult zeroVelocity() {
        return CollisionUtils.handlePhysics(FLOOR_GETTER, PLAYER_BB, restPos, Vec.ZERO, cachedResult, false);
    }

    @Benchmark
    public PhysicsResult cachedStanding() {
        return CollisionUtils.handlePhysics(FLOOR_GETTER, PLAYER_BB, restPos, restVelocity, cachedResult, false);
    }

    @Benchmark
    public PhysicsResult fallThroughAir() {
        return CollisionUtils.handlePhysics(AIR_GETTER, PLAYER_BB, new Pos(0.5, 100.0, 0.5),
                new Vec(0, -0.4, 0), null, false);
    }

    @Benchmark
    public PhysicsResult fallIntoFloor() {
        return CollisionUtils.handlePhysics(FLOOR_GETTER, PLAYER_BB, new Pos(0.5, 64.3, 0.5),
                new Vec(0, -0.4, 0), null, false);
    }

    @Benchmark
    public PhysicsResult walkOnFloor() {
        return CollisionUtils.handlePhysics(FLOOR_GETTER, PLAYER_BB, new Pos(0.5, 64.0, 0.5),
                new Vec(0.2, -0.08, 0.15), null, false);
    }

    @Benchmark
    public PhysicsResult diagonalMove() {
        return CollisionUtils.handlePhysics(FLOOR_GETTER, PLAYER_BB, new Pos(0.5, 64.0, 0.5),
                new Vec(1, 0, 1), null, false);
    }

    @Benchmark
    public PhysicsResult largeMoveSlow() {
        // length ~3.3, not diagonal -> slowPhysics ray-cast that crosses the floor
        return CollisionUtils.handlePhysics(FLOOR_GETTER, PLAYER_BB, new Pos(0.5, 67.0, 0.5),
                new Vec(1.5, -2.5, 1.5), null, false);
    }

    @Benchmark
    public PhysicsResult fenceCollision() {
        // Standing atop the fence layer (top at y=64.5), walking into the adjacent fences.
        return CollisionUtils.handlePhysics(FENCE_GETTER, PLAYER_BB, new Pos(0.5, 64.5, 0.5),
                new Vec(0.3, -0.08, 0.0), null, false);
    }

    @Benchmark
    public PhysicsResult denseCollision() {
        // Embedded in solid stone with a small velocity: collisions on all three axes,
        // multiple iterations of the stepPhysics while-loop.
        return CollisionUtils.handlePhysics(DENSE_GETTER, PLAYER_BB, new Pos(0.5, 64.5, 0.5),
                new Vec(0.3, -0.3, 0.3), null, false);
    }

    // --- locking-getter variants: measure block-lookup cost (dedup benefit) ---

    @Benchmark
    public PhysicsResult walkOnFloorLocking() {
        return CollisionUtils.handlePhysics(LOCKING_FLOOR_GETTER, PLAYER_BB, new Pos(0.5, 64.0, 0.5),
                new Vec(0.2, -0.08, 0.15), null, false);
    }

    @Benchmark
    public PhysicsResult denseCollisionLocking() {
        return CollisionUtils.handlePhysics(LOCKING_DENSE_GETTER, PLAYER_BB, new Pos(0.5, 64.5, 0.5),
                new Vec(0.3, -0.3, 0.3), null, false);
    }

    @Benchmark
    public PhysicsResult fallThroughAirLocking() {
        return CollisionUtils.handlePhysics(LOCKING_AIR_GETTER, PLAYER_BB, new Pos(0.5, 100.0, 0.5),
                new Vec(0, -0.4, 0), null, false);
    }

    @Benchmark
    public PhysicsResult largeMoveSlowLocking() {
        return CollisionUtils.handlePhysics(LOCKING_FLOOR_GETTER, PLAYER_BB, new Pos(0.5, 67.0, 0.5),
                new Vec(1.5, -2.5, 1.5), null, false);
    }

    // --- simulateMovement paths (handlePhysics + world border + velocity update) ---

    @Benchmark
    public PhysicsResult simulateOnGround() {
        return PhysicsUtils.simulateMovement(new Pos(0.5, 64.0, 0.5), new Vec(0.1, 0, 0.1), PLAYER_BB,
                BORDER, FLOOR_GETTER, AERO, false, true, true, false, null);
    }

    @Benchmark
    public PhysicsResult simulateFalling() {
        return PhysicsUtils.simulateMovement(new Pos(0.5, 100.0, 0.5), new Vec(0.1, -0.4, 0.1), PLAYER_BB,
                BORDER, AIR_GETTER, AERO, false, true, false, false, null);
    }

    // --- helpers ---

    @FunctionalInterface
    private interface BlockAt {
        Block get(int x, int y, int z);
    }

    private static Block.Getter condGetter(BlockAt fn) {
        return (x, y, z, condition) -> fn.get(x, y, z);
    }

    private static Block.Getter lockingGetter(BlockAt fn) {
        final java.util.concurrent.locks.ReentrantReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();
        return (x, y, z, condition) -> {
            lock.readLock().lock();
            try {
                return fn.get(x, y, z);
            } finally {
                lock.readLock().unlock();
            }
        };
    }
}
