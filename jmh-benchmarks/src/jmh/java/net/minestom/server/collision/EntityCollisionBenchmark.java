package net.minestom.server.collision;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.EntityTracker;
import org.openjdk.jmh.annotations.*;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Scaling baseline for {@link EntityCollision#checkCollision}: the entity-vs-entity collision query
 * run for every colliding entity every tick. Exercises the whole path - the {@link EntityTracker}
 * broad-phase chunk scan plus the per-candidate sweep narrow-phase.
 * <p>
 * {@code entityCount} entities are packed inside the query's search range around the mover (so the
 * broad-phase returns all of them and every one is swept) and spread across several chunks. The
 * mover advances with a moderate velocity, giving a realistic mix of overlaps, swept hits and
 * misses. Vary the param to see how the query scales with crowd density - the number to watch when
 * optimizing.
 */
@BenchmarkMode({Mode.AverageTime, Mode.Throughput})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 8, time = 1)
@Fork(2)
public class EntityCollisionBenchmark {
    private static final BoundingBox MOVER_BB = new BoundingBox(0.6, 1.8, 0.6);
    private static final Pos MOVER_POS = new Pos(0.5, 64.0, 0.5);
    private static final Vec VELOCITY = new Vec(0.42, 0.0, 0.42); // ~0.6/tick forward
    private static final double EXTEND_RADIUS = 1.51;
    // Pack inside the broad-phase range (extendRadius + maxDistance + |velocity| ~= 3.6) so every
    // entity is returned and swept; the disk spans ~4 chunks, exercising the multi-chunk scan.
    private static final double PACK_RADIUS = 3.0;

    @Param({"16", "128", "1024"})
    private int entityCount;

    private EntityTracker tracker;

    @Setup
    public void setup() {
        final Random rnd = new Random(1234567);
        tracker = EntityTracker.newTracker();
        for (int i = 0; i < entityCount; i++) {
            final double angle = rnd.nextDouble() * 2 * Math.PI;
            final double dist = rnd.nextDouble() * PACK_RADIUS;
            final Pos pos = new Pos(MOVER_POS.x() + Math.cos(angle) * dist, 64.0, MOVER_POS.z() + Math.sin(angle) * dist);
            tracker.register(new PositionedEntity(pos), pos, EntityTracker.Target.ENTITIES, null);
        }
    }

    @Benchmark
    public Collection<EntityCollisionResult> checkCollisions() {
        return EntityCollision.checkCollision(tracker, MOVER_BB, MOVER_POS, VELOCITY,
                EXTEND_RADIUS, _ -> true, null);
    }

    private static final class PositionedEntity extends Entity {
        PositionedEntity(Pos pos) {
            super(EntityType.ZOMBIE);
            this.boundingBox = MOVER_BB;
            this.position = pos;
        }
    }
}
