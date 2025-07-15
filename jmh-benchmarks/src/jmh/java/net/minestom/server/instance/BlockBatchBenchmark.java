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
    
    // Areas for getBlockBatch benchmarks
    private Area smallArea;
    private Area mediumArea;
    private Area largeArea;
    private Area crossChunkArea;
    private Area multiSectionArea;

    @Setup
    public void setup() {
        MinecraftServer.init();
        instance = new InstanceContainer(UUID.randomUUID(), DimensionType.OVERWORLD);

        // Pre-load chunks
        final int range = 10;
        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                instance.loadChunk(x, z).join();
            }
        }
        
        // Initialize areas
        smallArea = Area.cuboid(Vec.ZERO, new Vec(7, 7, 7)); // 8x8x8 = 512 blocks
        mediumArea = Area.cuboid(Vec.ZERO, new Vec(15, 15, 15)); // 16x16x16 = 4096 blocks
        largeArea = Area.cuboid(Vec.ZERO, new Vec(31, 31, 31)); // 32x32x32 = 32768 blocks
        crossChunkArea = Area.cuboid(new Vec(8, 0, 8), new Vec(23, 15, 23)); // Cross chunk boundaries
        multiSectionArea = Area.cuboid(new Vec(0, 0, 0), new Vec(15, 63, 15)); // Multiple Y sections
        
        // Fill instance with some blocks for getBlockBatch tests
        fillInstanceWithBlocks();
        
        // Pre-build batches
        createSmallBatches();
        createMediumBatches();
        createLargeBatches();
    }
    
    private void fillInstanceWithBlocks() {
        // Fill with a pattern of blocks for realistic getBlockBatch tests
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 64; y++) {
                for (int z = 0; z < 64; z++) {
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
        // Small batches: 8x8x8 = 512 blocks
        smallUnalignedBatch = BlockBatch.unaligned(builder -> {
            for (int x = 0; x < 8; x++) {
                for (int y = 0; y < 8; y++) {
                    for (int z = 0; z < 8; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });
        
        smallAlignedBatch = BlockBatch.aligned(builder -> {
            for (int x = 0; x < 8; x++) {
                for (int y = 0; y < 8; y++) {
                    for (int z = 0; z < 8; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });
        
        smallUnalignedStatesBatch = BlockBatch.unalignedStates(builder -> {
            for (int x = 0; x < 8; x++) {
                for (int y = 0; y < 8; y++) {
                    for (int z = 0; z < 8; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });
        
        smallAlignedStatesBatch = BlockBatch.alignedStates(builder -> {
            for (int x = 0; x < 8; x++) {
                for (int y = 0; y < 8; y++) {
                    for (int z = 0; z < 8; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });
    }
    
    private void createMediumBatches() {
        // Medium batches: 16x16x16 = 4096 blocks
        mediumUnalignedBatch = BlockBatch.unaligned(builder -> {
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });
        
        mediumAlignedBatch = BlockBatch.aligned(builder -> {
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });
        
        mediumUnalignedStatesBatch = BlockBatch.unalignedStates(builder -> {
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });
        
        mediumAlignedStatesBatch = BlockBatch.alignedStates(builder -> {
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });
    }
    
    private void createLargeBatches() {
        // Large batches: 32x32x32 = 32768 blocks
        largeUnalignedBatch = BlockBatch.unaligned(builder -> {
            for (int x = 0; x < 32; x++) {
                for (int y = 0; y < 32; y++) {
                    for (int z = 0; z < 32; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });
        
        largeAlignedBatch = BlockBatch.aligned(builder -> {
            for (int x = 0; x < 32; x++) {
                for (int y = 0; y < 32; y++) {
                    for (int z = 0; z < 32; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });
        
        largeUnalignedStatesBatch = BlockBatch.unalignedStates(builder -> {
            for (int x = 0; x < 32; x++) {
                for (int y = 0; y < 32; y++) {
                    for (int z = 0; z < 32; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });
        
        largeAlignedStatesBatch = BlockBatch.alignedStates(builder -> {
            for (int x = 0; x < 32; x++) {
                for (int y = 0; y < 32; y++) {
                    for (int z = 0; z < 32; z++) {
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
            for (int x = 0; x < 8; x++) {
                for (int y = 0; y < 8; y++) {
                    for (int z = 0; z < 8; z++) {
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
            for (int x = 0; x < 8; x++) {
                for (int y = 0; y < 8; y++) {
                    for (int z = 0; z < 8; z++) {
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
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
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
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
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
        instance.setBlockBatch(100, 10, 100, smallUnalignedBatch);
    }
    
    @Benchmark
    public void setBlockBatchSmallAlignedOrigin() {
        instance.setBlockBatch(100, 10, 100, smallAlignedBatch);
    }
    
    @Benchmark
    public void setBlockBatchSmallUnalignedAlignedOrigin() {
        instance.setBlockBatch(96, 16, 96, smallUnalignedBatch); // Aligned to section boundaries
    }
    
    @Benchmark
    public void setBlockBatchSmallAlignedAlignedOrigin() {
        instance.setBlockBatch(96, 16, 96, smallAlignedBatch); // Aligned to section boundaries
    }
    
    @Benchmark
    public void setBlockBatchMediumUnalignedOrigin() {
        instance.setBlockBatch(100, 10, 100, mediumUnalignedBatch);
    }
    
    @Benchmark
    public void setBlockBatchMediumAlignedOrigin() {
        instance.setBlockBatch(100, 10, 100, mediumAlignedBatch);
    }
    
    @Benchmark
    public void setBlockBatchMediumUnalignedAlignedOrigin() {
        instance.setBlockBatch(96, 16, 96, mediumUnalignedBatch); // Aligned to section boundaries
    }
    
    @Benchmark
    public void setBlockBatchMediumAlignedAlignedOrigin() {
        instance.setBlockBatch(96, 16, 96, mediumAlignedBatch); // Aligned to section boundaries
    }
    
    @Benchmark
    public void setBlockBatchLargeUnalignedOrigin() {
        instance.setBlockBatch(100, 10, 100, largeUnalignedBatch);
    }
    
    @Benchmark
    public void setBlockBatchLargeAlignedOrigin() {
        instance.setBlockBatch(100, 10, 100, largeAlignedBatch);
    }
    
    @Benchmark
    public void setBlockBatchLargeUnalignedAlignedOrigin() {
        instance.setBlockBatch(96, 16, 96, largeUnalignedBatch); // Aligned to section boundaries
    }
    
    @Benchmark
    public void setBlockBatchLargeAlignedAlignedOrigin() {
        instance.setBlockBatch(96, 16, 96, largeAlignedBatch); // Aligned to section boundaries
    }

    // ========================== STATES VS FULL BLOCKS COMPARISON ==========================
    
    @Benchmark
    public void setBlockBatchSmallUnalignedStates() {
        instance.setBlockBatch(100, 10, 100, smallUnalignedStatesBatch);
    }
    
    @Benchmark
    public void setBlockBatchSmallAlignedStates() {
        instance.setBlockBatch(100, 10, 100, smallAlignedStatesBatch);
    }
    
    @Benchmark
    public void setBlockBatchMediumUnalignedStates() {
        instance.setBlockBatch(100, 10, 100, mediumUnalignedStatesBatch);
    }
    
    @Benchmark
    public void setBlockBatchMediumAlignedStates() {
        instance.setBlockBatch(100, 10, 100, mediumAlignedStatesBatch);
    }

    // ========================== DIRECT SETBLOCK COMPARISON ==========================
    
    @Benchmark
    public void directSetBlockSmall() {
        int baseX = 100, baseY = 10, baseZ = 100;
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                for (int z = 0; z < 8; z++) {
                    instance.setBlock(baseX + x, baseY + y, baseZ + z, 
                                    (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                }
            }
        }
    }
    
    @Benchmark
    public void directSetBlockMedium() {
        int baseX = 100, baseY = 10, baseZ = 100;
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    instance.setBlock(baseX + x, baseY + y, baseZ + z, 
                                    (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                }
            }
        }
    }
    
    @Benchmark
    public void directSetBlockLarge() {
        int baseX = 100, baseY = 10, baseZ = 100;
        for (int x = 0; x < 32; x++) {
            for (int y = 0; y < 32; y++) {
                for (int z = 0; z < 32; z++) {
                    instance.setBlock(baseX + x, baseY + y, baseZ + z, 
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
        BlockBatch batch = instance.getBlockBatch(new Vec(5, 3, 7), smallArea);
        bh.consume(batch);
    }
    
    @Benchmark
    public void getBlockBatchAlignedOrigin(Blackhole bh) {
        BlockBatch batch = instance.getBlockBatch(new Vec(16, 16, 16), smallArea);
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
    public void readBlockBatchSmallIndividual(Blackhole bh) {
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                for (int z = 0; z < 8; z++) {
                    Block block = smallUnalignedBatch.getBlock(x, y, z);
                    bh.consume(block);
                }
            }
        }
    }
    
    @Benchmark
    public void readBlockBatchMediumIndividual(Blackhole bh) {
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
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
        BlockBatch batch = instance.getBlockBatch(new Vec(0, 0, 0), smallArea);
        instance.setBlockBatch(150, 10, 150, batch);
        bh.consume(batch);
    }
    
    @Benchmark
    public void roundtripMediumBatch(Blackhole bh) {
        // Get a batch from the instance, then set it back
        BlockBatch batch = instance.getBlockBatch(new Vec(0, 0, 0), mediumArea);
        instance.setBlockBatch(150, 10, 150, batch);
        bh.consume(batch);
    }

    // ========================== CROSS-CHUNK AND MULTI-SECTION BENCHMARKS ==========================
    
    @Benchmark
    public void setBlockBatchCrossChunk() {
        // Create a batch that spans multiple chunks
        BlockBatch batch = BlockBatch.unaligned(builder -> {
            for (int x = 0; x < 24; x++) {
                for (int y = 0; y < 8; y++) {
                    for (int z = 0; z < 24; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });
        instance.setBlockBatch(8, 10, 8, batch); // Starts at chunk boundary, spans 3x3 chunks
    }
    
    @Benchmark
    public void setBlockBatchMultiSection() {
        // Create a batch that spans multiple Y sections
        BlockBatch batch = BlockBatch.unaligned(builder -> {
            for (int x = 0; x < 8; x++) {
                for (int y = 0; y < 48; y++) {
                    for (int z = 0; z < 8; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });
        instance.setBlockBatch(100, 0, 100, batch); // Spans multiple Y sections
    }
    
    @Benchmark
    public void setBlockBatchSectionAligned() {
        // Test with batch that aligns perfectly with sections
        BlockBatch batch = BlockBatch.aligned(builder -> {
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        builder.setBlock(x, y, z, (x + y + z) % 2 == 0 ? Block.STONE : Block.DIRT);
                    }
                }
            }
        });
        instance.setBlockBatch(80, 32, 80, batch); // Perfectly aligned to section boundaries
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
}