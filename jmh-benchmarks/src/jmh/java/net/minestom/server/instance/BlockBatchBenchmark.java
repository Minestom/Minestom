package net.minestom.server.instance;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Area;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.world.DimensionType;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, warmups = 1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class BlockBatchBenchmark {

    // Size constants
    private static final int SMALL_SIZE = 8;
    private static final int MEDIUM_SIZE = 16;
    private static final int LARGE_SIZE = 32;
    private static final int HUGE_SIZE = 128;

    // Coordinate constants
    private static final int CHUNK_LOADING_RANGE = 10;
    private static final int FILL_AREA_SIZE = 144;

    // Test origin coordinates
    private static final int UNALIGNED_ORIGIN_X = 100;
    private static final int UNALIGNED_ORIGIN_Y = 10;
    private static final int UNALIGNED_ORIGIN_Z = 100;

    private static final int ALIGNED_ORIGIN_X = 96;
    private static final int ALIGNED_ORIGIN_Y = 16;
    private static final int ALIGNED_ORIGIN_Z = 96;

    private static final int HUGE_ALIGNED_ORIGIN_X = 512;
    private static final int HUGE_ALIGNED_ORIGIN_Y = 0;
    private static final int HUGE_ALIGNED_ORIGIN_Z = 512;

    // Cross-chunk test coordinates
    private static final int CROSS_CHUNK_START_X = 8;
    private static final int CROSS_CHUNK_START_Y = 0;
    private static final int CROSS_CHUNK_START_Z = 8;
    private static final int CROSS_CHUNK_END_X = 23;
    private static final int CROSS_CHUNK_END_Y = 15;
    private static final int CROSS_CHUNK_END_Z = 23;

    // Multi-section test coordinates
    private static final int MULTI_SECTION_END_Y = 63;

    // Alternative test origins
    private static final int ALT_ORIGIN_X = 150;
    private static final int ALT_ORIGIN_Y = 10;
    private static final int ALT_ORIGIN_Z = 150;

    private static final int HUGE_ALT_ORIGIN_X = 1500;
    private static final int HUGE_ALT_ORIGIN_Y = 10;
    private static final int HUGE_ALT_ORIGIN_Z = 1500;

    // Unaligned test origins
    private static final int SMALL_UNALIGNED_X = 5;
    private static final int SMALL_UNALIGNED_Y = 3;
    private static final int SMALL_UNALIGNED_Z = 7;

    // Section aligned test origins
    private static final int SECTION_ALIGNED_X = 80;
    private static final int SECTION_ALIGNED_Y = 32;
    private static final int SECTION_ALIGNED_Z = 80;

    private InstanceContainer instance;

    // Pre-built batches for reuse
    private BlockBatch smallUnalignedBatch;
    private BlockBatch smallAlignedBatch;
    private BlockBatch smallUnalignedStatesBatch;
    private BlockBatch smallAlignedStatesBatch;

    private BlockBatch mediumUnalignedBatch;
    private BlockBatch mediumAlignedBatch;
    private BlockBatch mediumUnalignedStatesBatch;
    private BlockBatch mediumAlignedStatesBatch;

    private BlockBatch largeUnalignedBatch;
    private BlockBatch largeAlignedBatch;
    private BlockBatch largeUnalignedStatesBatch;
    private BlockBatch largeAlignedStatesBatch;

    private BlockBatch hugeUnalignedBatch;
    private BlockBatch hugeAlignedBatch;
    private BlockBatch hugeUnalignedStatesBatch;
    private BlockBatch hugeAlignedStatesBatch;

    // Areas for getBlockBatch benchmarks
    private Area smallArea;
    private Area mediumArea;
    private Area largeArea;
    private Area hugeArea;
    private Area crossChunkArea;
    private Area multiSectionArea;

    @Setup
    public void setup() {
        MinecraftServer.init();
        instance = new InstanceContainer(UUID.randomUUID(), DimensionType.OVERWORLD);

        // Pre-load chunks
        for (int x = -CHUNK_LOADING_RANGE; x <= CHUNK_LOADING_RANGE; x++) {
            for (int z = -CHUNK_LOADING_RANGE; z <= CHUNK_LOADING_RANGE; z++) {
                instance.loadChunk(x, z).join();
            }
        }

        // Initialize areas
        smallArea = Area.cuboid(Vec.ZERO, new Vec(SMALL_SIZE - 1, SMALL_SIZE - 1, SMALL_SIZE - 1));
        mediumArea = Area.cuboid(Vec.ZERO, new Vec(MEDIUM_SIZE - 1, MEDIUM_SIZE - 1, MEDIUM_SIZE - 1));
        largeArea = Area.cuboid(Vec.ZERO, new Vec(LARGE_SIZE - 1, LARGE_SIZE - 1, LARGE_SIZE - 1));
        hugeArea = Area.cuboid(Vec.ZERO, new Vec(HUGE_SIZE - 1, HUGE_SIZE - 1, HUGE_SIZE - 1));
        crossChunkArea = Area.cuboid(new Vec(CROSS_CHUNK_START_X, CROSS_CHUNK_START_Y, CROSS_CHUNK_START_Z),
                new Vec(CROSS_CHUNK_END_X, CROSS_CHUNK_END_Y, CROSS_CHUNK_END_Z));
        multiSectionArea = Area.cuboid(new Vec(0, 0, 0), new Vec(MEDIUM_SIZE - 1, MULTI_SECTION_END_Y, MEDIUM_SIZE - 1));

        // Fill instance with some blocks for getBlockBatch tests
        fillInstanceWithBlocks();

        // Pre-build batches
        createSmallBatches();
        createMediumBatches();
        createLargeBatches();
        createHugeBatches();
    }

    private void fillInstanceWithBlocks() {
        // Fill with a pattern of blocks for realistic getBlockBatch tests
        // Fill a reasonable area that covers the test regions
        for (int x = 0; x < FILL_AREA_SIZE; x++) {
            for (int y = 0; y < FILL_AREA_SIZE; y++) {
                for (int z = 0; z < FILL_AREA_SIZE; z++) {
                    Block block = switch ((x + y + z) % 4) {
                        case 0 -> Block.STONE;
                        case 1 -> Block.DIRT;
                        case 2 -> Block.GRASS_BLOCK;
                        default -> Block.COBBLESTONE;
                    };
                    instance.setBlock(x, y, z, block);
                }
            }
        }
    }

    private void createSmallBatches() {
        // Small batches: SMALL_SIZE^3 blocks
        smallUnalignedBatch = BlockBatch.unaligned(builder -> {
            for (int x = 0; x < SMALL_SIZE; x++) {
                for (int y = 0; y < SMALL_SIZE; y++) {
                    for (int z = 0; z < SMALL_SIZE; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });

        smallAlignedBatch = BlockBatch.aligned(builder -> {
            for (int x = 0; x < SMALL_SIZE; x++) {
                for (int y = 0; y < SMALL_SIZE; y++) {
                    for (int z = 0; z < SMALL_SIZE; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });

        smallUnalignedStatesBatch = BlockBatch.unalignedStates(builder -> {
            for (int x = 0; x < SMALL_SIZE; x++) {
                for (int y = 0; y < SMALL_SIZE; y++) {
                    for (int z = 0; z < SMALL_SIZE; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });

        smallAlignedStatesBatch = BlockBatch.alignedStates(builder -> {
            for (int x = 0; x < SMALL_SIZE; x++) {
                for (int y = 0; y < SMALL_SIZE; y++) {
                    for (int z = 0; z < SMALL_SIZE; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });
    }

    private void createMediumBatches() {
        // Medium batches: MEDIUM_SIZE^3 blocks
        mediumUnalignedBatch = BlockBatch.unaligned(builder -> {
            for (int x = 0; x < MEDIUM_SIZE; x++) {
                for (int y = 0; y < MEDIUM_SIZE; y++) {
                    for (int z = 0; z < MEDIUM_SIZE; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });

        mediumAlignedBatch = BlockBatch.aligned(builder -> {
            for (int x = 0; x < MEDIUM_SIZE; x++) {
                for (int y = 0; y < MEDIUM_SIZE; y++) {
                    for (int z = 0; z < MEDIUM_SIZE; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });

        mediumUnalignedStatesBatch = BlockBatch.unalignedStates(builder -> {
            for (int x = 0; x < MEDIUM_SIZE; x++) {
                for (int y = 0; y < MEDIUM_SIZE; y++) {
                    for (int z = 0; z < MEDIUM_SIZE; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });

        mediumAlignedStatesBatch = BlockBatch.alignedStates(builder -> {
            for (int x = 0; x < MEDIUM_SIZE; x++) {
                for (int y = 0; y < MEDIUM_SIZE; y++) {
                    for (int z = 0; z < MEDIUM_SIZE; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });
    }

    private void createLargeBatches() {
        // Large batches: LARGE_SIZE^3 blocks
        largeUnalignedBatch = BlockBatch.unaligned(builder -> {
            for (int x = 0; x < LARGE_SIZE; x++) {
                for (int y = 0; y < LARGE_SIZE; y++) {
                    for (int z = 0; z < LARGE_SIZE; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });

        largeAlignedBatch = BlockBatch.aligned(builder -> {
            for (int x = 0; x < LARGE_SIZE; x++) {
                for (int y = 0; y < LARGE_SIZE; y++) {
                    for (int z = 0; z < LARGE_SIZE; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });

        largeUnalignedStatesBatch = BlockBatch.unalignedStates(builder -> {
            for (int x = 0; x < LARGE_SIZE; x++) {
                for (int y = 0; y < LARGE_SIZE; y++) {
                    for (int z = 0; z < LARGE_SIZE; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });

        largeAlignedStatesBatch = BlockBatch.alignedStates(builder -> {
            for (int x = 0; x < LARGE_SIZE; x++) {
                for (int y = 0; y < LARGE_SIZE; y++) {
                    for (int z = 0; z < LARGE_SIZE; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });
    }

    private void createHugeBatches() {
        // Huge batches: HUGE_SIZE^3 blocks
        hugeUnalignedBatch = BlockBatch.unaligned(builder -> {
            for (int x = 0; x < HUGE_SIZE; x++) {
                for (int y = 0; y < HUGE_SIZE; y++) {
                    for (int z = 0; z < HUGE_SIZE; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });

        hugeAlignedBatch = BlockBatch.aligned(builder -> {
            for (int x = 0; x < HUGE_SIZE; x++) {
                for (int y = 0; y < HUGE_SIZE; y++) {
                    for (int z = 0; z < HUGE_SIZE; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });

        hugeUnalignedStatesBatch = BlockBatch.unalignedStates(builder -> {
            for (int x = 0; x < HUGE_SIZE; x++) {
                for (int y = 0; y < HUGE_SIZE; y++) {
                    for (int z = 0; z < HUGE_SIZE; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });

        hugeAlignedStatesBatch = BlockBatch.alignedStates(builder -> {
            for (int x = 0; x < HUGE_SIZE; x++) {
                for (int y = 0; y < HUGE_SIZE; y++) {
                    for (int z = 0; z < HUGE_SIZE; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });
    }

    // ========================== BATCH CREATION BENCHMARKS ==========================

    @Benchmark
    public void createSmallUnalignedBatch(Blackhole bh) {
        BlockBatch batch = BlockBatch.unaligned(builder -> {
            for (int x = 0; x < SMALL_SIZE; x++) {
                for (int y = 0; y < SMALL_SIZE; y++) {
                    for (int z = 0; z < SMALL_SIZE; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });
        bh.consume(batch);
    }

    @Benchmark
    public void createSmallAlignedBatch(Blackhole bh) {
        BlockBatch batch = BlockBatch.aligned(builder -> {
            for (int x = 0; x < SMALL_SIZE; x++) {
                for (int y = 0; y < SMALL_SIZE; y++) {
                    for (int z = 0; z < SMALL_SIZE; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });
        bh.consume(batch);
    }

    @Benchmark
    public void createMediumUnalignedBatch(Blackhole bh) {
        BlockBatch batch = BlockBatch.unaligned(builder -> {
            for (int x = 0; x < MEDIUM_SIZE; x++) {
                for (int y = 0; y < MEDIUM_SIZE; y++) {
                    for (int z = 0; z < MEDIUM_SIZE; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });
        bh.consume(batch);
    }

    @Benchmark
    public void createMediumAlignedBatch(Blackhole bh) {
        BlockBatch batch = BlockBatch.aligned(builder -> {
            for (int x = 0; x < MEDIUM_SIZE; x++) {
                for (int y = 0; y < MEDIUM_SIZE; y++) {
                    for (int z = 0; z < MEDIUM_SIZE; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });
        bh.consume(batch);
    }

    @Benchmark
    public void createLargeUnalignedBatch(Blackhole bh) {
        BlockBatch batch = BlockBatch.unaligned(builder -> {
            for (int x = 0; x < LARGE_SIZE; x++) {
                for (int y = 0; y < LARGE_SIZE; y++) {
                    for (int z = 0; z < LARGE_SIZE; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });
        bh.consume(batch);
    }

    @Benchmark
    public void createLargeAlignedBatch(Blackhole bh) {
        BlockBatch batch = BlockBatch.aligned(builder -> {
            for (int x = 0; x < LARGE_SIZE; x++) {
                for (int y = 0; y < LARGE_SIZE; y++) {
                    for (int z = 0; z < LARGE_SIZE; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });
        bh.consume(batch);
    }

    @Benchmark
    public void createHugeUnalignedBatch(Blackhole bh) {
        BlockBatch batch = BlockBatch.unaligned(builder -> {
            for (int x = 0; x < HUGE_SIZE; x++) {
                for (int y = 0; y < HUGE_SIZE; y++) {
                    for (int z = 0; z < HUGE_SIZE; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });
        bh.consume(batch);
    }

    @Benchmark
    public void createHugeAlignedBatch(Blackhole bh) {
        BlockBatch batch = BlockBatch.aligned(builder -> {
            for (int x = 0; x < HUGE_SIZE; x++) {
                for (int y = 0; y < HUGE_SIZE; y++) {
                    for (int z = 0; z < HUGE_SIZE; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });
        bh.consume(batch);
    }

    // ========================== SETBLOCKBATCH BENCHMARKS ==========================

    @Benchmark
    public void setBlockBatchSmallUnalignedOrigin() {
        instance.setBlockBatch(UNALIGNED_ORIGIN_X, UNALIGNED_ORIGIN_Y, UNALIGNED_ORIGIN_Z, smallUnalignedBatch);
    }

    @Benchmark
    public void setBlockBatchSmallAlignedOrigin() {
        instance.setBlockBatch(UNALIGNED_ORIGIN_X, UNALIGNED_ORIGIN_Y, UNALIGNED_ORIGIN_Z, smallAlignedBatch);
    }

    @Benchmark
    public void setBlockBatchSmallUnalignedAlignedOrigin() {
        instance.setBlockBatch(ALIGNED_ORIGIN_X, ALIGNED_ORIGIN_Y, ALIGNED_ORIGIN_Z, smallUnalignedBatch);
    }

    @Benchmark
    public void setBlockBatchSmallAlignedAlignedOrigin() {
        instance.setBlockBatch(ALIGNED_ORIGIN_X, ALIGNED_ORIGIN_Y, ALIGNED_ORIGIN_Z, smallAlignedBatch);
    }

    @Benchmark
    public void setBlockBatchMediumUnalignedOrigin() {
        instance.setBlockBatch(UNALIGNED_ORIGIN_X, UNALIGNED_ORIGIN_Y, UNALIGNED_ORIGIN_Z, mediumUnalignedBatch);
    }

    @Benchmark
    public void setBlockBatchMediumAlignedOrigin() {
        instance.setBlockBatch(UNALIGNED_ORIGIN_X, UNALIGNED_ORIGIN_Y, UNALIGNED_ORIGIN_Z, mediumAlignedBatch);
    }

    @Benchmark
    public void setBlockBatchMediumUnalignedAlignedOrigin() {
        instance.setBlockBatch(ALIGNED_ORIGIN_X, ALIGNED_ORIGIN_Y, ALIGNED_ORIGIN_Z, mediumUnalignedBatch);
    }

    @Benchmark
    public void setBlockBatchMediumAlignedAlignedOrigin() {
        instance.setBlockBatch(ALIGNED_ORIGIN_X, ALIGNED_ORIGIN_Y, ALIGNED_ORIGIN_Z, mediumAlignedBatch);
    }

    @Benchmark
    public void setBlockBatchLargeUnalignedOrigin() {
        instance.setBlockBatch(UNALIGNED_ORIGIN_X, UNALIGNED_ORIGIN_Y, UNALIGNED_ORIGIN_Z, largeUnalignedBatch);
    }

    @Benchmark
    public void setBlockBatchLargeAlignedOrigin() {
        instance.setBlockBatch(UNALIGNED_ORIGIN_X, UNALIGNED_ORIGIN_Y, UNALIGNED_ORIGIN_Z, largeAlignedBatch);
    }

    @Benchmark
    public void setBlockBatchLargeUnalignedAlignedOrigin() {
        instance.setBlockBatch(ALIGNED_ORIGIN_X, ALIGNED_ORIGIN_Y, ALIGNED_ORIGIN_Z, largeUnalignedBatch);
    }

    @Benchmark
    public void setBlockBatchLargeAlignedAlignedOrigin() {
        instance.setBlockBatch(ALIGNED_ORIGIN_X, ALIGNED_ORIGIN_Y, ALIGNED_ORIGIN_Z, largeAlignedBatch);
    }

    @Benchmark
    public void setBlockBatchHugeUnalignedOrigin() {
        instance.setBlockBatch(UNALIGNED_ORIGIN_X, UNALIGNED_ORIGIN_Y, UNALIGNED_ORIGIN_Z, hugeUnalignedBatch);
    }

    @Benchmark
    public void setBlockBatchHugeAlignedOrigin() {
        instance.setBlockBatch(UNALIGNED_ORIGIN_X, UNALIGNED_ORIGIN_Y, UNALIGNED_ORIGIN_Z, hugeAlignedBatch);
    }

    @Benchmark
    public void setBlockBatchHugeUnalignedAlignedOrigin() {
        instance.setBlockBatch(HUGE_ALIGNED_ORIGIN_X, HUGE_ALIGNED_ORIGIN_Y, HUGE_ALIGNED_ORIGIN_Z, hugeUnalignedBatch);
    }

    @Benchmark
    public void setBlockBatchHugeAlignedAlignedOrigin() {
        instance.setBlockBatch(HUGE_ALIGNED_ORIGIN_X, HUGE_ALIGNED_ORIGIN_Y, HUGE_ALIGNED_ORIGIN_Z, hugeAlignedBatch);
    }

    @Benchmark
    public void setBlockBatchHugeUnalignedStates() {
        instance.setBlockBatch(UNALIGNED_ORIGIN_X, UNALIGNED_ORIGIN_Y, UNALIGNED_ORIGIN_Z, hugeUnalignedStatesBatch);
    }

    @Benchmark
    public void setBlockBatchHugeAlignedStates() {
        instance.setBlockBatch(UNALIGNED_ORIGIN_X, UNALIGNED_ORIGIN_Y, UNALIGNED_ORIGIN_Z, hugeAlignedStatesBatch);
    }

    // ========================== STATES VS FULL BLOCKS COMPARISON ==========================

    @Benchmark
    public void setBlockBatchSmallUnalignedStates() {
        instance.setBlockBatch(UNALIGNED_ORIGIN_X, UNALIGNED_ORIGIN_Y, UNALIGNED_ORIGIN_Z, smallUnalignedStatesBatch);
    }

    @Benchmark
    public void setBlockBatchSmallAlignedStates() {
        instance.setBlockBatch(UNALIGNED_ORIGIN_X, UNALIGNED_ORIGIN_Y, UNALIGNED_ORIGIN_Z, smallAlignedStatesBatch);
    }

    @Benchmark
    public void setBlockBatchMediumUnalignedStates() {
        instance.setBlockBatch(UNALIGNED_ORIGIN_X, UNALIGNED_ORIGIN_Y, UNALIGNED_ORIGIN_Z, mediumUnalignedStatesBatch);
    }

    @Benchmark
    public void setBlockBatchMediumAlignedStates() {
        instance.setBlockBatch(UNALIGNED_ORIGIN_X, UNALIGNED_ORIGIN_Y, UNALIGNED_ORIGIN_Z, mediumAlignedStatesBatch);
    }

    @Benchmark
    public void setBlockBatchLargeUnalignedStates() {
        instance.setBlockBatch(UNALIGNED_ORIGIN_X, UNALIGNED_ORIGIN_Y, UNALIGNED_ORIGIN_Z, largeUnalignedStatesBatch);
    }

    @Benchmark
    public void setBlockBatchLargeAlignedStates() {
        instance.setBlockBatch(UNALIGNED_ORIGIN_X, UNALIGNED_ORIGIN_Y, UNALIGNED_ORIGIN_Z, largeAlignedStatesBatch);
    }

    // ========================== DIRECT SETBLOCK COMPARISON ==========================

    @Benchmark
    public void directSetBlockSmall() {
        for (int x = 0; x < SMALL_SIZE; x++) {
            for (int y = 0; y < SMALL_SIZE; y++) {
                for (int z = 0; z < SMALL_SIZE; z++) {
                    instance.setBlock(UNALIGNED_ORIGIN_X + x, UNALIGNED_ORIGIN_Y + y, UNALIGNED_ORIGIN_Z + z,
                            (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                }
            }
        }
    }

    @Benchmark
    public void directSetBlockMedium() {
        for (int x = 0; x < MEDIUM_SIZE; x++) {
            for (int y = 0; y < MEDIUM_SIZE; y++) {
                for (int z = 0; z < MEDIUM_SIZE; z++) {
                    instance.setBlock(UNALIGNED_ORIGIN_X + x, UNALIGNED_ORIGIN_Y + y, UNALIGNED_ORIGIN_Z + z,
                            (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                }
            }
        }
    }

    @Benchmark
    public void directSetBlockLarge() {
        for (int x = 0; x < LARGE_SIZE; x++) {
            for (int y = 0; y < LARGE_SIZE; y++) {
                for (int z = 0; z < LARGE_SIZE; z++) {
                    instance.setBlock(UNALIGNED_ORIGIN_X + x, UNALIGNED_ORIGIN_Y + y, UNALIGNED_ORIGIN_Z + z,
                            (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                }
            }
        }
    }

    @Benchmark
    public void directSetBlockHuge() {
        for (int x = 0; x < HUGE_SIZE; x++) {
            for (int y = 0; y < HUGE_SIZE; y++) {
                for (int z = 0; z < HUGE_SIZE; z++) {
                    instance.setBlock(UNALIGNED_ORIGIN_X + x, UNALIGNED_ORIGIN_Y + y, UNALIGNED_ORIGIN_Z + z,
                            (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                }
            }
        }
    }

    // ========================== GETBLOCKBATCH BENCHMARKS ==========================

    @Benchmark
    public void getBlockBatchSmallArea(Blackhole bh) {
        BlockBatch batch = instance.getBlockBatch(Vec.ZERO, smallArea);
        bh.consume(batch);
    }

    @Benchmark
    public void getBlockBatchSmallAreaStatesOnly(Blackhole bh) {
        BlockBatch batch = instance.getBlockBatch(BlockBatch.IGNORE_DATA_FLAG, Vec.ZERO, smallArea);
        bh.consume(batch);
    }

    @Benchmark
    public void getBlockBatchMediumArea(Blackhole bh) {
        BlockBatch batch = instance.getBlockBatch(Vec.ZERO, mediumArea);
        bh.consume(batch);
    }

    @Benchmark
    public void getBlockBatchMediumAreaStatesOnly(Blackhole bh) {
        BlockBatch batch = instance.getBlockBatch(BlockBatch.IGNORE_DATA_FLAG, Vec.ZERO, mediumArea);
        bh.consume(batch);
    }

    @Benchmark
    public void getBlockBatchLargeArea(Blackhole bh) {
        BlockBatch batch = instance.getBlockBatch(Vec.ZERO, largeArea);
        bh.consume(batch);
    }

    @Benchmark
    public void getBlockBatchCrossChunk(Blackhole bh) {
        BlockBatch batch = instance.getBlockBatch(Vec.ZERO, crossChunkArea);
        bh.consume(batch);
    }

    @Benchmark
    public void getBlockBatchMultiSection(Blackhole bh) {
        BlockBatch batch = instance.getBlockBatch(Vec.ZERO, multiSectionArea);
        bh.consume(batch);
    }

    @Benchmark
    public void getBlockBatchUnalignedOrigin(Blackhole bh) {
        BlockBatch batch = instance.getBlockBatch(new Vec(SMALL_UNALIGNED_X, SMALL_UNALIGNED_Y, SMALL_UNALIGNED_Z), smallArea);
        bh.consume(batch);
    }

    @Benchmark
    public void getBlockBatchAlignedOrigin(Blackhole bh) {
        BlockBatch batch = instance.getBlockBatch(new Vec(MEDIUM_SIZE, MEDIUM_SIZE, MEDIUM_SIZE), smallArea);
        bh.consume(batch);
    }

    @Benchmark
    public void getBlockBatchHugeArea(Blackhole bh) {
        BlockBatch batch = instance.getBlockBatch(Vec.ZERO, hugeArea);
        bh.consume(batch);
    }

    @Benchmark
    public void getBlockBatchHugeAreaStatesOnly(Blackhole bh) {
        BlockBatch batch = instance.getBlockBatch(BlockBatch.IGNORE_DATA_FLAG, Vec.ZERO, hugeArea);
        bh.consume(batch);
    }

    // ========================== BATCH READING BENCHMARKS ==========================

    @Benchmark
    public void readBlockBatchSmallGetAll(Blackhole bh) {
        smallUnalignedBatch.getAll((x, y, z, block) -> bh.consume(block));
    }

    @Benchmark
    public void readBlockBatchMediumGetAll(Blackhole bh) {
        mediumUnalignedBatch.getAll((x, y, z, block) -> bh.consume(block));
    }

    @Benchmark
    public void readBlockBatchLargeGetAll(Blackhole bh) {
        largeUnalignedBatch.getAll((x, y, z, block) -> bh.consume(block));
    }

    @Benchmark
    public void readBlockBatchHugeGetAll(Blackhole bh) {
        hugeUnalignedBatch.getAll((x, y, z, block) -> bh.consume(block));
    }

    @Benchmark
    public void readBlockBatchSmallIndividual(Blackhole bh) {
        for (int x = 0; x < SMALL_SIZE; x++) {
            for (int y = 0; y < SMALL_SIZE; y++) {
                for (int z = 0; z < SMALL_SIZE; z++) {
                    Block block = smallUnalignedBatch.getBlock(x, y, z);
                    bh.consume(block);
                }
            }
        }
    }

    @Benchmark
    public void readBlockBatchMediumIndividual(Blackhole bh) {
        for (int x = 0; x < MEDIUM_SIZE; x++) {
            for (int y = 0; y < MEDIUM_SIZE; y++) {
                for (int z = 0; z < MEDIUM_SIZE; z++) {
                    Block block = mediumUnalignedBatch.getBlock(x, y, z);
                    bh.consume(block);
                }
            }
        }
    }

    // ========================== ROUNDTRIP BENCHMARKS ==========================

    @Benchmark
    public void roundtripSmallBatch(Blackhole bh) {
        // Get a batch from the instance, then set it back
        BlockBatch batch = instance.getBlockBatch(Vec.ZERO, smallArea);
        instance.setBlockBatch(ALT_ORIGIN_X, ALT_ORIGIN_Y, ALT_ORIGIN_Z, batch);
        bh.consume(batch);
    }

    @Benchmark
    public void roundtripMediumBatch(Blackhole bh) {
        // Get a batch from the instance, then set it back
        BlockBatch batch = instance.getBlockBatch(Vec.ZERO, mediumArea);
        instance.setBlockBatch(ALT_ORIGIN_X, ALT_ORIGIN_Y, ALT_ORIGIN_Z, batch);
        bh.consume(batch);
    }

    @Benchmark
    public void roundtripHugeBatch(Blackhole bh) {
        // Get a batch from the instance, then set it back
        BlockBatch batch = instance.getBlockBatch(Vec.ZERO, hugeArea);
        instance.setBlockBatch(HUGE_ALT_ORIGIN_X, HUGE_ALT_ORIGIN_Y, HUGE_ALT_ORIGIN_Z, batch);
        bh.consume(batch);
    }

    // ========================== CROSS-CHUNK AND MULTI-SECTION BENCHMARKS ==========================

    @Benchmark
    public void setBlockBatchCrossChunk() {
        // Create a batch that spans multiple chunks
        BlockBatch batch = BlockBatch.unaligned(builder -> {
            int spanX = CROSS_CHUNK_END_X - CROSS_CHUNK_START_X + 1;
            int spanY = CROSS_CHUNK_END_Y - CROSS_CHUNK_START_Y + 1;
            int spanZ = CROSS_CHUNK_END_Z - CROSS_CHUNK_START_Z + 1;
            for (int x = 0; x < spanX; x++) {
                for (int y = 0; y < spanY; y++) {
                    for (int z = 0; z < spanZ; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });
        instance.setBlockBatch(CROSS_CHUNK_START_X, CROSS_CHUNK_START_Y, CROSS_CHUNK_START_Z, batch);
    }

    @Benchmark
    public void setBlockBatchMultiSection() {
        // Create a batch that spans multiple Y sections
        BlockBatch batch = BlockBatch.unaligned(builder -> {
            for (int x = 0; x < SMALL_SIZE; x++) {
                for (int y = 0; y < (MULTI_SECTION_END_Y + 1); y++) {
                    for (int z = 0; z < SMALL_SIZE; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });
        instance.setBlockBatch(UNALIGNED_ORIGIN_X, 0, UNALIGNED_ORIGIN_Z, batch);
    }

    @Benchmark
    public void setBlockBatchSectionAligned() {
        // Test with batch that aligns perfectly with sections
        BlockBatch batch = BlockBatch.aligned(builder -> {
            for (int x = 0; x < MEDIUM_SIZE; x++) {
                for (int y = 0; y < MEDIUM_SIZE; y++) {
                    for (int z = 0; z < MEDIUM_SIZE; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });
        instance.setBlockBatch(SECTION_ALIGNED_X, SECTION_ALIGNED_Y, SECTION_ALIGNED_Z, batch);
    }

    // ========================== BATCH COUNT AND STATISTICS BENCHMARKS ==========================

    @Benchmark
    public void batchCountSmall(Blackhole bh) {
        int count = smallUnalignedBatch.count();
        bh.consume(count);
    }

    @Benchmark
    public void batchCountMedium(Blackhole bh) {
        int count = mediumUnalignedBatch.count();
        bh.consume(count);
    }

    @Benchmark
    public void batchCountLarge(Blackhole bh) {
        int count = largeUnalignedBatch.count();
        bh.consume(count);
    }

    @Benchmark
    public void batchCountHuge(Blackhole bh) {
        int count = hugeUnalignedBatch.count();
        bh.consume(count);
    }

    @Benchmark
    public void batchFlagsSmall(Blackhole bh) {
        long flags = smallUnalignedBatch.flags();
        boolean ignoreData = smallUnalignedBatch.ignoreData();
        boolean aligned = smallUnalignedBatch.aligned();
        boolean generate = smallUnalignedBatch.generate();
        bh.consume(flags);
        bh.consume(ignoreData);
        bh.consume(aligned);
        bh.consume(generate);
    }

    // ========================== DIRECT GETBLOCK COMPARISON ==========================

    @Benchmark
    public void directGetBlockSmall(Blackhole bh) {
        // Read the same area as smallArea
        for (int x = 0; x < SMALL_SIZE; x++) {
            for (int y = 0; y < SMALL_SIZE; y++) {
                for (int z = 0; z < SMALL_SIZE; z++) {
                    Block block = instance.getBlock(x, y, z);
                    bh.consume(block);
                }
            }
        }
    }

    @Benchmark
    public void directGetBlockMedium(Blackhole bh) {
        // Read the same area as mediumArea
        for (int x = 0; x < MEDIUM_SIZE; x++) {
            for (int y = 0; y < MEDIUM_SIZE; y++) {
                for (int z = 0; z < MEDIUM_SIZE; z++) {
                    Block block = instance.getBlock(x, y, z);
                    bh.consume(block);
                }
            }
        }
    }

    @Benchmark
    public void directGetBlockLarge(Blackhole bh) {
        // Read the same area as largeArea
        for (int x = 0; x < LARGE_SIZE; x++) {
            for (int y = 0; y < LARGE_SIZE; y++) {
                for (int z = 0; z < LARGE_SIZE; z++) {
                    Block block = instance.getBlock(x, y, z);
                    bh.consume(block);
                }
            }
        }
    }

    @Benchmark
    public void directGetBlockCrossChunk(Blackhole bh) {
        // Read the same area as crossChunkArea
        for (int x = CROSS_CHUNK_START_X; x <= CROSS_CHUNK_END_X; x++) {
            for (int y = CROSS_CHUNK_START_Y; y <= CROSS_CHUNK_END_Y; y++) {
                for (int z = CROSS_CHUNK_START_Z; z <= CROSS_CHUNK_END_Z; z++) {
                    Block block = instance.getBlock(x, y, z);
                    bh.consume(block);
                }
            }
        }
    }

    @Benchmark
    public void directGetBlockMultiSection(Blackhole bh) {
        // Read the same area as multiSectionArea
        for (int x = 0; x < MEDIUM_SIZE; x++) {
            for (int y = 0; y <= MULTI_SECTION_END_Y; y++) {
                for (int z = 0; z < MEDIUM_SIZE; z++) {
                    Block block = instance.getBlock(x, y, z);
                    bh.consume(block);
                }
            }
        }
    }

    @Benchmark
    public void directGetBlockUnalignedOrigin(Blackhole bh) {
        // Read from unaligned origin with small area size
        for (int x = SMALL_UNALIGNED_X; x < SMALL_UNALIGNED_X + SMALL_SIZE; x++) {
            for (int y = SMALL_UNALIGNED_Y; y < SMALL_UNALIGNED_Y + SMALL_SIZE; y++) {
                for (int z = SMALL_UNALIGNED_Z; z < SMALL_UNALIGNED_Z + SMALL_SIZE; z++) {
                    Block block = instance.getBlock(x, y, z);
                    bh.consume(block);
                }
            }
        }
    }

    @Benchmark
    public void directGetBlockAlignedOrigin(Blackhole bh) {
        // Read from aligned origin with small area size
        for (int x = MEDIUM_SIZE; x < MEDIUM_SIZE + SMALL_SIZE; x++) {
            for (int y = MEDIUM_SIZE; y < MEDIUM_SIZE + SMALL_SIZE; y++) {
                for (int z = MEDIUM_SIZE; z < MEDIUM_SIZE + SMALL_SIZE; z++) {
                    Block block = instance.getBlock(x, y, z);
                    bh.consume(block);
                }
            }
        }
    }

    @Benchmark
    public void directGetBlockWithConditionSmall(Blackhole bh) {
        // Test with different condition types
        for (int x = 0; x < SMALL_SIZE; x++) {
            for (int y = 0; y < SMALL_SIZE; y++) {
                for (int z = 0; z < SMALL_SIZE; z++) {
                    Block block = instance.getBlock(x, y, z, Block.Getter.Condition.TYPE);
                    bh.consume(block);
                }
            }
        }
    }

    @Benchmark
    public void directGetBlockCachedConditionSmall(Blackhole bh) {
        // Test with CACHED condition
        for (int x = 0; x < SMALL_SIZE; x++) {
            for (int y = 0; y < SMALL_SIZE; y++) {
                for (int z = 0; z < SMALL_SIZE; z++) {
                    Block block = instance.getBlock(x, y, z, Block.Getter.Condition.CACHED);
                    bh.consume(block);
                }
            }
        }
    }

    // ========================== GENERATOR CONVERSION BENCHMARKS ==========================

    @Benchmark
    public void batchAsGeneratorSmall(Blackhole bh) {
        var generator = smallUnalignedBatch.asGenerator();
        bh.consume(generator);
    }

    @Benchmark
    public void batchAsGeneratorMedium(Blackhole bh) {
        var generator = mediumUnalignedBatch.asGenerator();
        bh.consume(generator);
    }

    @Benchmark
    public void batchAsGeneratorHuge(Blackhole bh) {
        var generator = hugeUnalignedBatch.asGenerator();
        bh.consume(generator);
    }
}