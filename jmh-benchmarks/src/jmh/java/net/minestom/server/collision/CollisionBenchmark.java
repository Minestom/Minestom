package net.minestom.server.collision;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.Nullable;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(2)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
public class CollisionBenchmark {

    @Benchmark
    public void blockPhysics(BlockCollisionState state, Blackhole blackhole) {
        blackhole.consume(state.run());
    }

    @Benchmark
    public void entityCollision(EntityCollisionState state, Blackhole blackhole) {
        blackhole.consume(state.run());
    }

    @State(Scope.Benchmark)
    public static class BlockCollisionState {
        @Param({
                "empty_short",
                "floor_gravity",
                "wall_horizontal",
                "corner_diagonal",
                "ceiling_vertical",
                "long_sweep",
                "slab_subblock",
                "fence_tall"
        })
        public String collisionCase;

        private StaticBlockGetter getter;
        private BoundingBox boundingBox;
        private Pos position;
        private Vec velocity;

        @Setup
        public void setup() {
            getter = new StaticBlockGetter();
            boundingBox = new BoundingBox(0.6, 1.8, 0.6);

            switch (collisionCase) {
                case "empty_short" -> {
                    position = new Pos(0, 42, 0);
                    velocity = new Vec(0.3, -0.08, 0.3);
                }
                case "floor_gravity" -> {
                    fill(getter, -1, 40, -1, 1, 40, 1, Block.STONE);
                    position = new Pos(0, 42, 0);
                    velocity = new Vec(0, -4, 0);
                }
                case "wall_horizontal" -> {
                    fill(getter, -1, 42, 1, 1, 44, 1, Block.STONE);
                    position = new Pos(0, 42, 0);
                    velocity = new Vec(0, 0, 2);
                }
                case "corner_diagonal" -> {
                    fill(getter, 1, 42, 0, 1, 44, 1, Block.STONE);
                    fill(getter, 0, 42, 1, 1, 44, 1, Block.STONE);
                    position = new Pos(0, 42, 0);
                    velocity = new Vec(2, 0, 2);
                }
                case "ceiling_vertical" -> {
                    fill(getter, -1, 44, -1, 1, 44, 1, Block.STONE);
                    position = new Pos(0, 42, 0);
                    velocity = new Vec(0, 5, 0);
                }
                case "long_sweep" -> {
                    fill(getter, 48, 42, -1, 48, 44, 1, Block.STONE);
                    position = new Pos(0, 42, 0);
                    velocity = new Vec(64, 0, 0);
                }
                case "slab_subblock" -> {
                    fill(getter, -1, 41, -1, 1, 41, 1, Block.STONE_SLAB);
                    position = new Pos(0, 42, 0);
                    velocity = new Vec(0.4, -1.2, 0.4);
                }
                case "fence_tall" -> {
                    getter.set(0, 41, 0, Block.OAK_FENCE);
                    position = new Pos(0, 42, -0.7);
                    velocity = new Vec(0, -0.4, 1.2);
                }
                default -> throw new IllegalArgumentException("Unknown collision case: " + collisionCase);
            }
        }

        public PhysicsResult run() {
            return CollisionUtils.handlePhysics(getter, boundingBox, position, velocity, false);
        }
    }

    @State(Scope.Benchmark)
    public static class EntityCollisionState {
        @Param({
                "single_hit",
                "multi_hit",
                "overlap",
                "miss_many",
                "filtered_many"
        })
        public String collisionCase;

        private Instance instance;
        private Entity movingEntity;
        private Vec velocity;

        @Setup
        public void setup() {
            MinecraftServer.init();
            instance = MinecraftServer.getInstanceManager().createInstanceContainer();
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    instance.loadChunk(x, z).join();
                }
            }

            movingEntity = spawn(new Pos(0, 42, 0));

            switch (collisionCase) {
                case "single_hit" -> {
                    spawn(new Pos(0, 42, 1));
                    spawn(new Pos(0, 42, 3));
                    velocity = new Vec(0, 0, 1);
                }
                case "multi_hit" -> {
                    spawn(new Pos(0, 42, 1));
                    spawn(new Pos(0, 42, 2));
                    spawn(new Pos(0, 42, 3));
                    velocity = new Vec(0, 0, 3);
                }
                case "overlap" -> {
                    spawn(new Pos(0, 42, 0));
                    velocity = new Vec(0, 0, 1);
                }
                case "miss_many" -> {
                    for (int i = 0; i < 48; i++) {
                        spawn(new Pos((i % 8) + 4, 42, ((double) i / 8) + 4));
                    }
                    velocity = new Vec(0, 0, 1);
                }
                case "filtered_many" -> {
                    for (int i = 0; i < 48; i++) {
                        spawn(new Pos(0, 42, i + 1));
                    }
                    velocity = new Vec(0, 0, 48);
                }
                default -> throw new IllegalArgumentException("Unknown collision case: " + collisionCase);
            }
        }

        @TearDown
        public void tearDown() {
            MinecraftServer.stopCleanly();
        }

        public Collection<EntityCollisionResult> run() {
            return switch (collisionCase) {
                case "filtered_many" ->
                        CollisionUtils.checkEntityCollisions(movingEntity, velocity, 1.51, _ -> false, null);
                default ->
                        CollisionUtils.checkEntityCollisions(movingEntity, velocity, 1.51, entity -> entity != movingEntity, null);
            };
        }

        private Entity spawn(Pos position) {
            Entity entity = new Entity(EntityType.ZOMBIE);
            entity.setInstance(instance, position).join();
            return entity;
        }
    }

    private static void fill(StaticBlockGetter getter,
                             int minX, int minY, int minZ,
                             int maxX, int maxY, int maxZ,
                             Block block) {
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    getter.set(x, y, z, block);
                }
            }
        }
    }

    private static final class StaticBlockGetter implements Block.Getter {
        private final Long2ObjectOpenHashMap<Block> blocks = new Long2ObjectOpenHashMap<>();

        private StaticBlockGetter() {
            blocks.defaultReturnValue(Block.AIR);
        }

        private void set(int x, int y, int z, Block block) {
            blocks.put(CoordConversion.hashBlockCoord(x, y, z), block);
        }

        @Override
        public Block getBlock(int x, int y, int z, @Nullable Condition condition) {
            return blocks.get(CoordConversion.hashBlockCoord(x, y, z));
        }
    }
}
