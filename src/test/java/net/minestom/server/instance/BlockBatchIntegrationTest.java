package net.minestom.server.instance;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.coordinate.Area;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static net.minestom.server.coordinate.Area.cuboid;
import static net.minestom.server.coordinate.CoordConversion.SECTION_BLOCK_COUNT;
import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class BlockBatchIntegrationTest {

    @Test
    public void basicUnaligned(Env env) {
        var instance = env.createEmptyInstance();
        BlockBatch batch = BlockBatch.unaligned(builder -> builder.setBlock(0, 0, 0, Block.STONE));
        instance.setBlockBatch(Vec.ONE, batch);
        assertEquals(Block.STONE, instance.getBlock(1, 1, 1));
        assertEquals(Block.AIR, instance.getBlock(0, 0, 0));
    }

    @Test
    public void basicAligned(Env env) {
        var instance = env.createEmptyInstance();
        BlockBatch batch = BlockBatch.aligned(builder -> builder.setBlock(0, 0, 0, Block.STONE));
        instance.setBlockBatch(Vec.ONE, batch);
        assertEquals(Block.STONE, instance.getBlock(1, 1, 1));
        assertEquals(Block.AIR, instance.getBlock(0, 0, 0));
    }

    @Test
    public void multipleBlocksUnaligned(Env env) {
        var instance = env.createEmptyInstance();
        BlockBatch batch = BlockBatch.unaligned(builder -> {
            builder.setBlock(0, 0, 0, Block.STONE);
            builder.setBlock(1, 0, 0, Block.DIRT);
            builder.setBlock(0, 1, 0, Block.GRASS_BLOCK);
            builder.setBlock(2, 2, 2, Block.DIAMOND_BLOCK);
        });

        instance.setBlockBatch(10, 10, 10, batch);

        assertEquals(Block.STONE, instance.getBlock(10, 10, 10));
        assertEquals(Block.DIRT, instance.getBlock(11, 10, 10));
        assertEquals(Block.GRASS_BLOCK, instance.getBlock(10, 11, 10));
        assertEquals(Block.DIAMOND_BLOCK, instance.getBlock(12, 12, 12));
        assertEquals(Block.AIR, instance.getBlock(9, 9, 9));
    }

    @Test
    public void multipleBlocksAligned(Env env) {
        var instance = env.createEmptyInstance();
        BlockBatch batch = BlockBatch.aligned(builder -> {
            builder.setBlock(0, 0, 0, Block.STONE);
            builder.setBlock(15, 0, 0, Block.DIRT);
            builder.setBlock(0, 15, 0, Block.GRASS_BLOCK);
            builder.setBlock(15, 15, 15, Block.DIAMOND_BLOCK);
        });

        instance.setBlockBatch(16, 16, 16, batch);

        assertEquals(Block.STONE, instance.getBlock(16, 16, 16));
        assertEquals(Block.DIRT, instance.getBlock(31, 16, 16));
        assertEquals(Block.GRASS_BLOCK, instance.getBlock(16, 31, 16));
        assertEquals(Block.DIAMOND_BLOCK, instance.getBlock(31, 31, 31));
    }

    @Test
    public void largeBatch(Env env) {
        var instance = env.createEmptyInstance();

        // Create a large batch with many blocks
        BlockBatch batch = BlockBatch.aligned(builder -> {
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        if ((x + y + z) % 2 == 0) {
                            builder.setBlock(x, y, z, Block.STONE);
                        } else {
                            builder.setBlock(x, y, z, Block.DIRT);
                        }
                    }
                }
            }
        });

        instance.setBlockBatch(0, 0, 0, batch);

        // Verify blocks were set correctly
        assertEquals(Block.STONE, instance.getBlock(0, 0, 0)); // even sum
        assertEquals(Block.DIRT, instance.getBlock(1, 0, 0)); // odd sum
        assertEquals(Block.STONE, instance.getBlock(2, 0, 0)); // even sum
        assertEquals(Block.DIRT, instance.getBlock(0, 1, 0)); // odd sum
    }

    @Test
    public void multiSectionBatch(Env env) {
        var instance = env.createEmptyInstance();

        // Create a batch spanning multiple sections
        BlockBatch batch = BlockBatch.unaligned(builder -> {
            // Section 0,0,0
            builder.setBlock(0, 0, 0, Block.STONE);
            builder.setBlock(15, 15, 15, Block.DIRT);

            // Section 0,1,0 (Y+16)
            builder.setBlock(0, 16, 0, Block.GRASS_BLOCK);
            builder.setBlock(15, 31, 15, Block.DIAMOND_BLOCK);

            // Section 1,0,0 (X+16)
            builder.setBlock(16, 0, 0, Block.GOLD_BLOCK);
            builder.setBlock(31, 15, 15, Block.IRON_BLOCK);
        });

        instance.setBlockBatch(32, 32, 32, batch);

        // Verify all sections were processed
        assertEquals(Block.STONE, instance.getBlock(32, 32, 32));
        assertEquals(Block.DIRT, instance.getBlock(47, 47, 47));
        assertEquals(Block.GRASS_BLOCK, instance.getBlock(32, 48, 32));
        assertEquals(Block.DIAMOND_BLOCK, instance.getBlock(47, 63, 47));
        assertEquals(Block.GOLD_BLOCK, instance.getBlock(48, 32, 32));
        assertEquals(Block.IRON_BLOCK, instance.getBlock(63, 47, 47));
    }

    @Test
    public void crossChunkBatch(Env env) {
        var instance = env.createEmptyInstance();

        // Create a batch that crosses chunk boundaries
        BlockBatch batch = BlockBatch.unaligned(builder -> {
            // Chunk 0,0
            builder.setBlock(0, 0, 0, Block.STONE);
            builder.setBlock(15, 0, 15, Block.DIRT);

            // Chunk 1,0 (X+16)
            builder.setBlock(16, 0, 0, Block.GRASS_BLOCK);
            builder.setBlock(31, 0, 15, Block.DIAMOND_BLOCK);

            // Chunk 0,1 (Z+16)
            builder.setBlock(0, 0, 16, Block.GOLD_BLOCK);
            builder.setBlock(15, 0, 31, Block.IRON_BLOCK);

            // Chunk 1,1 (X+16, Z+16)
            builder.setBlock(16, 0, 16, Block.EMERALD_BLOCK);
            builder.setBlock(31, 0, 31, Block.REDSTONE_BLOCK);
        });

        instance.setBlockBatch(0, 64, 0, batch);

        // Verify blocks in all chunks
        assertEquals(Block.STONE, instance.getBlock(0, 64, 0));
        assertEquals(Block.DIRT, instance.getBlock(15, 64, 15));
        assertEquals(Block.GRASS_BLOCK, instance.getBlock(16, 64, 0));
        assertEquals(Block.DIAMOND_BLOCK, instance.getBlock(31, 64, 15));
        assertEquals(Block.GOLD_BLOCK, instance.getBlock(0, 64, 16));
        assertEquals(Block.IRON_BLOCK, instance.getBlock(15, 64, 31));
        assertEquals(Block.EMERALD_BLOCK, instance.getBlock(16, 64, 16));
        assertEquals(Block.REDSTONE_BLOCK, instance.getBlock(31, 64, 31));
    }

    @Test
    public void blocksWithNBT(Env env) {
        var instance = env.createEmptyInstance();

        // Create blocks with NBT data
        Block chestWithItems = Block.CHEST.withNbt(CompoundBinaryTag.builder()
                .putString("id", "minecraft:chest")
                .build());

        Block signWithText = Block.OAK_SIGN.withNbt(CompoundBinaryTag.builder()
                .putString("id", "minecraft:sign")
                .putString("Text1", "{\"text\":\"Hello\"}")
                .build());

        BlockBatch batch = BlockBatch.unaligned(builder -> {
            builder.setBlock(0, 0, 0, chestWithItems);
            builder.setBlock(1, 0, 0, signWithText);
            builder.setBlock(2, 0, 0, Block.STONE); // Regular block
        });

        instance.setBlockBatch(50, 50, 50, batch);

        // Verify blocks with NBT are preserved
        assertEquals(chestWithItems, instance.getBlock(50, 50, 50));
        assertEquals(signWithText, instance.getBlock(51, 50, 50));
        assertEquals(Block.STONE, instance.getBlock(52, 50, 50));
    }

    @Test
    public void emptyBatch(Env env) {
        var instance = env.createEmptyInstance();

        // Set some initial blocks
        instance.setBlock(10, 10, 10, Block.STONE);

        // Apply empty batch
        BlockBatch emptyBatch = BlockBatch.unaligned(builder -> {
            // No blocks added
        });

        instance.setBlockBatch(5, 5, 5, emptyBatch);

        // Original block should remain unchanged
        assertEquals(Block.STONE, instance.getBlock(10, 10, 10));
    }

    @Test
    public void overwriteExistingBlocks(Env env) {
        var instance = env.createEmptyInstance();

        // Set initial blocks
        instance.setBlock(0, 0, 0, Block.STONE);
        instance.setBlock(1, 0, 0, Block.DIRT);
        instance.setBlock(2, 0, 0, Block.GRASS_BLOCK);

        // Create batch that overwrites some blocks
        BlockBatch batch = BlockBatch.unaligned(builder -> {
            builder.setBlock(0, 0, 0, Block.DIAMOND_BLOCK); // Overwrite stone
            builder.setBlock(3, 0, 0, Block.GOLD_BLOCK); // New block
        });

        instance.setBlockBatch(0, 0, 0, batch);

        // Verify overwrite and preservation
        assertEquals(Block.DIAMOND_BLOCK, instance.getBlock(0, 0, 0)); // Overwritten
        assertEquals(Block.DIRT, instance.getBlock(1, 0, 0)); // Preserved
        assertEquals(Block.GRASS_BLOCK, instance.getBlock(2, 0, 0)); // Preserved
        assertEquals(Block.GOLD_BLOCK, instance.getBlock(3, 0, 0)); // New
    }

    @Test
    public void negativeCoordinates(Env env) {
        var instance = env.createEmptyInstance();

        BlockBatch batch = BlockBatch.unaligned(builder -> {
            builder.setBlock(0, 0, 0, Block.STONE);
            builder.setBlock(-1, -1, -1, Block.DIRT);
            builder.setBlock(5, 5, 5, Block.DIAMOND_BLOCK);
        });

        instance.setBlockBatch(-10, -10, -10, batch);

        assertEquals(Block.STONE, instance.getBlock(-10, -10, -10));
        assertEquals(Block.DIRT, instance.getBlock(-11, -11, -11));
        assertEquals(Block.DIAMOND_BLOCK, instance.getBlock(-5, -5, -5));
    }

    @Test
    public void batchConsistencyCheck(Env env) {
        var instance = env.createEmptyInstance();

        // Create identical batches using different methods
        BlockBatch explicitBatch = BlockBatch.unaligned(builder -> {
            for (int x = 0; x < 4; x++) {
                for (int y = 0; y < 4; y++) {
                    for (int z = 0; z < 4; z++) {
                        builder.setBlock(x, y, z, Block.STONE);
                    }
                }
            }
        });

        BlockBatch alignedBatch = BlockBatch.aligned(builder -> {
            for (int x = 0; x < 4; x++) {
                for (int y = 0; y < 4; y++) {
                    for (int z = 0; z < 4; z++) {
                        builder.setBlock(x, y, z, Block.STONE);
                    }
                }
            }
        });

        // Apply batches to different locations
        instance.setBlockBatch(0, 0, 0, explicitBatch);
        instance.setBlockBatch(10, 10, 10, alignedBatch);

        // Verify both produce the same result
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                for (int z = 0; z < 4; z++) {
                    assertEquals(Block.STONE, instance.getBlock(x, y, z));
                    assertEquals(Block.STONE, instance.getBlock(10 + x, 10 + y, 10 + z));
                }
            }
        }
    }

    @Test
    public void sectionStateSpansMultipleSections(Env env) {
        var instance = env.createEmptyInstance();

        // Create a batch with blocks in one section that will span multiple target sections
        // when an offset is applied
        BlockBatch batch = BlockBatch.unaligned(builder -> {
            // Fill most of a 16x16x16 section with blocks
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        builder.setBlock(x, y, z, Block.STONE);
                    }
                }
            }
        });

        // Apply with an offset that will cause blocks to span multiple sections
        // Offset of 8,8,8 means blocks will be split across 8 different sections
        instance.setBlockBatch(8, 8, 8, batch);

        // Verify blocks are correctly placed across section boundaries
        assertEquals(Block.STONE, instance.getBlock(8, 8, 8));    // Section (0,0,0)
        assertEquals(Block.STONE, instance.getBlock(15, 15, 15)); // Still section (0,0,0)
        assertEquals(Block.STONE, instance.getBlock(16, 16, 16)); // Section (1,1,1)
        assertEquals(Block.STONE, instance.getBlock(23, 23, 23)); // Still section (1,1,1)

        // Check edge cases at section boundaries
        assertEquals(Block.STONE, instance.getBlock(15, 8, 8));   // X boundary
        assertEquals(Block.STONE, instance.getBlock(16, 8, 8));   // X boundary + 1
        assertEquals(Block.STONE, instance.getBlock(8, 15, 8));   // Y boundary
        assertEquals(Block.STONE, instance.getBlock(8, 16, 8));   // Y boundary + 1
        assertEquals(Block.STONE, instance.getBlock(8, 8, 15));   // Z boundary
        assertEquals(Block.STONE, instance.getBlock(8, 8, 16));   // Z boundary + 1
    }

    @Test
    public void sectionAlignedWithOffsetSpansMultipleSections(Env env) {
        var instance = env.createEmptyInstance();

        // Create a section-aligned batch - this should use the optimized palette copying
        BlockBatch batch = BlockBatch.aligned(builder -> {
            // Fill a complete section
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        if ((x + y + z) % 2 == 0) {
                            builder.setBlock(x, y, z, Block.STONE);
                        } else {
                            builder.setBlock(x, y, z, Block.DIRT);
                        }
                    }
                }
            }
        });

        // Apply with offset that causes section boundary crossing
        instance.setBlockBatch(12, 12, 12, batch);

        // Verify the pattern is preserved across section boundaries
        assertEquals(Block.STONE, instance.getBlock(12, 12, 12)); // (0+0+0) % 2 == 0
        assertEquals(Block.DIRT, instance.getBlock(13, 12, 12));  // (1+0+0) % 2 == 1
        assertEquals(Block.DIRT, instance.getBlock(12, 13, 12));  // (0+1+0) % 2 == 1
        assertEquals(Block.STONE, instance.getBlock(14, 12, 12)); // (2+0+0) % 2 == 0

        // Check across section boundaries (at position 16)
        assertEquals(Block.STONE, instance.getBlock(16, 16, 16)); // Section boundary
        assertEquals(Block.DIRT, instance.getBlock(17, 16, 16));  // Across boundary
        assertEquals(Block.DIRT, instance.getBlock(27, 27, 27)); // End of batch
    }

    @Test
    public void blockStatesPreservedAcrossSections(Env env) {
        var instance = env.createEmptyInstance();

        // Create blocks with NBT that will span sections
        Block chestWithItems = Block.CHEST.withNbt(CompoundBinaryTag.builder()
                .putString("id", "minecraft:chest")
                .putString("Items", "[]")
                .build());

        Block signWithText = Block.OAK_SIGN.withNbt(CompoundBinaryTag.builder()
                .putString("id", "minecraft:sign")
                .putString("Text1", "{\"text\":\"Cross Section\"}")
                .build());

        BlockBatch batch = BlockBatch.unaligned(builder -> {
            // Place blocks that will end up in different sections due to offset
            builder.setBlock(0, 0, 0, chestWithItems);   // Will be at (10,10,10) - section (0,0,0)
            builder.setBlock(8, 8, 8, signWithText);     // Will be at (18,18,18) - section (1,1,1)
            builder.setBlock(15, 15, 15, Block.DIAMOND_BLOCK); // Will be at (25,25,25) - section (1,1,1)
        });

        instance.setBlockBatch(10, 10, 10, batch);

        // Verify blocks with NBT are correctly placed across sections
        Block placedChest = instance.getBlock(10, 10, 10);
        Block placedSign = instance.getBlock(18, 18, 18);
        Block placedDiamond = instance.getBlock(25, 25, 25);

        assertEquals(Block.CHEST, placedChest.withNbt(null));
        assertEquals(Block.OAK_SIGN, placedSign.withNbt(null));
        assertEquals(Block.DIAMOND_BLOCK, placedDiamond);

        // Verify NBT is preserved
        assertNotNull(placedChest.nbt());
        assertNotNull(placedSign.nbt());
        assertEquals("minecraft:chest", placedChest.nbt().getString("id"));
        assertEquals("minecraft:sign", placedSign.nbt().getString("id"));
    }

    @Test
    public void extremeOffsetHandling(Env env) {
        var instance = env.createEmptyInstance();

        // Create a batch with a pattern
        BlockBatch batch = BlockBatch.unaligned(builder -> {
            for (int x = 0; x < 32; x++) {
                for (int z = 0; z < 32; z++) {
                    builder.setBlock(x, 5, z, Block.STONE);
                }
            }
        });

        // Apply with extreme offset that will definitely span many sections
        instance.setBlockBatch(1000, 100, 2000, batch);

        // Verify blocks are placed correctly at extreme coordinates
        assertEquals(Block.STONE, instance.getBlock(1000, 105, 2000));
        assertEquals(Block.STONE, instance.getBlock(1015, 105, 2015)); // Section boundary
        assertEquals(Block.STONE, instance.getBlock(1016, 105, 2016)); // Across section
        assertEquals(Block.STONE, instance.getBlock(1031, 105, 2031)); // End of batch

        // Verify neighboring blocks are not affected
        instance.loadChunk(new Vec(1000, 105, 1999)).join();
        assertEquals(Block.AIR, instance.getBlock(999, 105, 2000));
        assertEquals(Block.AIR, instance.getBlock(1000, 105, 1999));
        assertEquals(Block.AIR, instance.getBlock(1000, 104, 2000));
        assertEquals(Block.AIR, instance.getBlock(1000, 106, 2000));
    }

    @Test
    public void mixedSectionTypesInBatch(Env env) {
        var instance = env.createEmptyInstance();

        // Create a batch that will have blocks in multiple sections
        BlockBatch batch = BlockBatch.unaligned(builder -> {
            // Section at (0,0,0) in batch coordinates
            for (int i = 0; i < 8; i++) {
                builder.setBlock(i, i, i, Block.STONE);
            }

            // Section at (1,0,0) in batch coordinates (X+16)
            for (int i = 0; i < 8; i++) {
                builder.setBlock(16 + i, i, i, Block.DIRT);
            }

            // Section at (0,1,0) in batch coordinates (Y+16)
            for (int i = 0; i < 8; i++) {
                builder.setBlock(i, 16 + i, i, Block.GRASS_BLOCK);
            }

            // Section at (0,0,1) in batch coordinates (Z+16)
            for (int i = 0; i < 8; i++) {
                builder.setBlock(i, i, 16 + i, Block.DIAMOND_BLOCK);
            }
        });

        // Apply with offset that will cause complex section mapping
        instance.setBlockBatch(7, 7, 7, batch);

        // Verify all sections are correctly applied
        // Original section (0,0,0) blocks
        assertEquals(Block.STONE, instance.getBlock(7, 7, 7));
        assertEquals(Block.STONE, instance.getBlock(14, 14, 14));

        // X+16 section blocks
        assertEquals(Block.DIRT, instance.getBlock(23, 7, 7));   // 7+16+0
        assertEquals(Block.DIRT, instance.getBlock(30, 14, 14)); // 7+16+7

        // Y+16 section blocks
        assertEquals(Block.GRASS_BLOCK, instance.getBlock(7, 23, 7));   // 7+0, 7+16+0, 7+0
        assertEquals(Block.GRASS_BLOCK, instance.getBlock(14, 30, 14)); // 7+7, 7+16+7, 7+7

        // Z+16 section blocks
        assertEquals(Block.DIAMOND_BLOCK, instance.getBlock(7, 7, 23));   // 7+0, 7+0, 7+16+0
        assertEquals(Block.DIAMOND_BLOCK, instance.getBlock(14, 14, 30)); // 7+7, 7+7, 7+16+7
    }

    @Test
    public void partialSectionOverlap(Env env) {
        var instance = env.createEmptyInstance();

        // Create a batch where only part of a section overlaps with target sections
        BlockBatch batch = BlockBatch.unaligned(builder -> {
            // Create a 10x10x10 cube starting at origin
            for (int x = 0; x < 10; x++) {
                for (int y = 0; y < 10; y++) {
                    for (int z = 0; z < 10; z++) {
                        builder.setBlock(x, y, z, Block.GOLD_BLOCK);
                    }
                }
            }
        });

        // Apply with offset that puts the cube partially in different sections
        instance.setBlockBatch(12, 12, 12, batch);

        // Verify blocks are placed correctly
        // All blocks should be gold blocks in a 10x10x10 cube starting at (12,12,12)
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                for (int z = 0; z < 10; z++) {
                    assertEquals(Block.GOLD_BLOCK, instance.getBlock(12 + x, 12 + y, 12 + z),
                            "Block at (" + (12 + x) + "," + (12 + y) + "," + (12 + z) + ") should be GOLD_BLOCK");
                }
            }
        }

        // Verify surrounding blocks are still air
        assertEquals(Block.AIR, instance.getBlock(11, 12, 12));
        assertEquals(Block.AIR, instance.getBlock(22, 12, 12));
        assertEquals(Block.AIR, instance.getBlock(12, 11, 12));
        assertEquals(Block.AIR, instance.getBlock(12, 22, 12));
        assertEquals(Block.AIR, instance.getBlock(12, 12, 11));
        assertEquals(Block.AIR, instance.getBlock(12, 12, 22));
    }

    @Test
    public void batchOverwritesNbtBlocks(Env env) {
        var instance = env.createEmptyInstance();

        // First, set a block with NBT manually
        Block chestWithItems = Block.CHEST.withNbt(CompoundBinaryTag.builder()
                .putString("id", "minecraft:chest")
                .putString("Items", "[{id:\"minecraft:diamond\",Count:1b}]")
                .build());

        Block signWithText = Block.OAK_SIGN.withNbt(CompoundBinaryTag.builder()
                .putString("id", "minecraft:sign")
                .putString("Text1", "{\"text\":\"Original Text\"}")
                .build());

        // Set blocks with NBT at specific positions
        instance.setBlock(10, 10, 10, chestWithItems);
        instance.setBlock(11, 10, 10, signWithText);
        instance.setBlock(12, 10, 10, Block.STONE); // Regular block for control

        // Verify the blocks with NBT are properly set
        Block placedChest = instance.getBlock(10, 10, 10);
        Block placedSign = instance.getBlock(11, 10, 10);
        assertEquals(Block.CHEST, placedChest.withNbt(null));
        assertEquals(Block.OAK_SIGN, placedSign.withNbt(null));
        assertNotNull(placedChest.nbt());
        assertNotNull(placedSign.nbt());
        assertEquals("minecraft:chest", placedChest.nbt().getString("id"));
        assertEquals("minecraft:sign", placedSign.nbt().getString("id"));

        // Create a batch that overwrites these blocks with blocks WITHOUT NBT
        BlockBatch batch = BlockBatch.unaligned(builder -> {
            builder.setBlock(0, 0, 0, Block.DIRT); // Overwrite chest
            builder.setBlock(1, 0, 0, Block.GRASS_BLOCK); // Overwrite sign
            builder.setBlock(2, 0, 0, Block.COBBLESTONE); // Overwrite stone
        });

        // Apply the batch at the same location
        instance.setBlockBatch(10, 10, 10, batch);

        // Verify that the blocks have been overwritten and NBT is removed
        Block newChestPos = instance.getBlock(10, 10, 10);
        Block newSignPos = instance.getBlock(11, 10, 10);
        Block newStonePos = instance.getBlock(12, 10, 10);

        assertEquals(Block.DIRT, newChestPos);
        assertEquals(Block.GRASS_BLOCK, newSignPos);
        assertEquals(Block.COBBLESTONE, newStonePos);

        // Most importantly, verify that no NBT remains
        assertNull(newChestPos.nbt());
        assertNull(newSignPos.nbt());
        assertNull(newStonePos.nbt());
    }

    @Test
    public void batchOverwritesNbtBlocksAligned(Env env) {
        var instance = env.createEmptyInstance();

        // Set blocks with NBT in a section-aligned manner
        Block chestWithItems = Block.CHEST.withNbt(CompoundBinaryTag.builder()
                .putString("id", "minecraft:chest")
                .putString("CustomName", "{\"text\":\"Test Chest\"}")
                .build());

        // Fill a 4x4x4 area with chests that have NBT
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                for (int z = 0; z < 4; z++) {
                    instance.setBlock(16 + x, 16 + y, 16 + z, chestWithItems);
                }
            }
        }

        // Verify some of the blocks have NBT
        Block testChest = instance.getBlock(16, 16, 16);
        assertEquals(Block.CHEST, testChest.withNbt(null));
        assertNotNull(testChest.nbt());
        assertEquals("minecraft:chest", testChest.nbt().getString("id"));

        // Create an aligned batch that overwrites with blocks without NBT
        BlockBatch batch = BlockBatch.aligned(builder -> {
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        if (x < 4 && y < 4 && z < 4) {
                            // Overwrite the chest area with stone (no NBT)
                            builder.setBlock(x, y, z, Block.STONE);
                        }
                    }
                }
            }
        });

        // Apply the batch
        instance.setBlockBatch(16, 16, 16, batch);

        // Verify that all the chests are now stone blocks without NBT
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                for (int z = 0; z < 4; z++) {
                    Block block = instance.getBlock(16 + x, 16 + y, 16 + z);
                    assertEquals(Block.STONE, block,
                            "Block at (" + (16 + x) + "," + (16 + y) + "," + (16 + z) + ") should be STONE");
                    assertNull(block.nbt(),
                            "Block at (" + (16 + x) + "," + (16 + y) + "," + (16 + z) + ") should not have NBT");
                }
            }
        }
    }

    @Test
    public void batchOverwritesNbtBlocksMixed(Env env) {
        var instance = env.createEmptyInstance();

        // Set blocks with NBT in a mixed manner
        Block chestWithItems = Block.CHEST.withNbt(CompoundBinaryTag.builder()
                .putString("id", "minecraft:chest")
                .putString("CustomName", "{\"text\":\"Test Chest\"}")
                .build());

        Block signWithText = Block.OAK_SIGN.withNbt(CompoundBinaryTag.builder()
                .putString("id", "minecraft:sign")
                .putString("Text1", "{\"text\":\"Original Text\"}")
                .build());

        // Set initial blocks with NBT
        instance.setBlock(0, 0, 0, chestWithItems);
        instance.setBlock(1, 0, 0, signWithText);

        // Create a batch that overwrites with blocks without NBT
        BlockBatch batch = BlockBatch.unaligned(builder -> {
            builder.setBlock(0, 0, 0, Block.DIRT); // Overwrite chest
            builder.setBlock(1, 0, 0, Block.GRASS_BLOCK); // Overwrite sign
        });

        // Apply the batch
        instance.setBlockBatch(0, 0, 0, batch);

        // Verify that the blocks have been overwritten and NBT is removed
        Block newChestPos = instance.getBlock(0, 0, 0);
        Block newSignPos = instance.getBlock(1, 0, 0);

        assertEquals(Block.DIRT, newChestPos);
        assertEquals(Block.GRASS_BLOCK, newSignPos);

        // Most importantly, verify that no NBT remains
        assertNull(newChestPos.nbt());
        assertNull(newSignPos.nbt());
    }

    @Test
    public void instanceGetBatch(Env env) {
        var instance = env.createEmptyInstance();

        Block chestWithItems = Block.CHEST;
        instance.setBlock(0, 0, 0, chestWithItems);

        BlockBatch batch = instance.getBlockBatch(Vec.ZERO, cuboid(Vec.ZERO, Vec.ZERO));
        assertNotNull(batch);
        assertEquals(1, batch.count());
        assertEquals(chestWithItems, batch.getBlock(0, 0, 0));
    }

    @Test
    public void instanceGetBatchState(Env env) {
        var instance = env.createEmptyInstance();

        Block chestWithItems = Block.CHEST.withNbt(CompoundBinaryTag.builder()
                .putString("id", "minecraft:chest")
                .putString("CustomName", "{\"text\":\"Test Chest\"}")
                .build());
        instance.setBlock(0, 0, 0, chestWithItems);

        BlockBatch batch = instance.getBlockBatch(Vec.ZERO, cuboid(Vec.ZERO, Vec.ZERO));
        assertNotNull(batch);
        assertEquals(1, batch.count());
        assertEquals(chestWithItems, batch.getBlock(0, 0, 0));
    }

    @Test
    public void instanceGetBatchAligned(Env env) {
        var instance = env.createEmptyInstance();

        Block chestWithItems = Block.CHEST;
        instance.setBlock(0, 0, 0, chestWithItems);

        BlockBatch batch = instance.getBlockBatch(Vec.ZERO, cuboid(Vec.ZERO, Vec.SECTION.sub(1)));
        assertNotNull(batch);
        assertEquals(SECTION_BLOCK_COUNT, batch.count());
        assertEquals(chestWithItems, batch.getBlock(0, 0, 0));
    }

    @Test
    public void instanceGetBatchStateAligned(Env env) {
        var instance = env.createEmptyInstance();

        Block chestWithItems = Block.CHEST.withNbt(CompoundBinaryTag.builder()
                .putString("id", "minecraft:chest")
                .putString("CustomName", "{\"text\":\"Test Chest\"}")
                .build());
        instance.setBlock(0, 0, 0, chestWithItems);

        BlockBatch batch = instance.getBlockBatch(Vec.ZERO, cuboid(Vec.ZERO, Vec.SECTION.sub(1)));
        assertNotNull(batch);
        assertEquals(SECTION_BLOCK_COUNT, batch.count());
        assertEquals(chestWithItems, batch.getBlock(0, 0, 0));
    }

    @Test
    public void instanceGetBatchOutboundHeight(Env env) {
        var instance = env.createEmptyInstance();

        Block chestWithItems = Block.CHEST.withNbt(CompoundBinaryTag.builder()
                .putString("id", "minecraft:chest")
                .putString("CustomName", "{\"text\":\"Test Chest\"}")
                .build());
        instance.setBlock(0, 32, 0, chestWithItems);
        instance.setBlock(0, 16, 0, chestWithItems);

        BlockBatch batch = instance.getBlockBatch(Vec.ZERO, cuboid(Vec.ZERO, Vec.SECTION.sub(1)));
        assertNotNull(batch);
        batch.getAll((x, y, z, block) -> assertEquals(Block.AIR, block));
    }

    @Test
    public void instanceGetBatchOrigin(Env env) {
        var instance = env.createEmptyInstance();

        Block block = Block.STONE;
        instance.setBlock(0, 0, 0, block);

        BlockBatch batch = instance.getBlockBatch(Vec.ONE, cuboid(Vec.ZERO, Vec.ZERO));
        assertNotNull(batch);
        assertEquals(1, batch.count());
        assertEquals(block, batch.getBlock(Vec.ONE.neg()));
    }

    @Test
    public void instanceGetBatchOriginAligned(Env env) {
        var instance = env.createEmptyInstance();

        Block block = Block.STONE;
        instance.setBlock(0, 0, 0, block);

        BlockBatch batch = instance.getBlockBatch(Vec.SECTION, cuboid(Vec.ZERO, Vec.ZERO));
        assertNotNull(batch);
        assertEquals(1, batch.count());
        batch.getAll((x, y, z, b) -> {
            final Vec expected = Vec.SECTION.neg();
            if (expected.samePoint(x, y, z)) {
                assertEquals(block, b, "Block at (" + x + "," + y + "," + z + ") should be CHEST");
            } else {
                assertEquals(Block.AIR, b, "Block at (" + x + "," + y + "," + z + ") should be AIR");
            }
        });
    }

    @Test
    public void instanceGetBatchSectionOriginAligned(Env env) {
        var instance = env.createEmptyInstance();

        Block block = Block.STONE;
        instance.setBlock(0, 0, 0, block);

        BlockBatch batch = instance.getBlockBatch(Vec.SECTION, cuboid(Vec.ZERO, Vec.SECTION.sub(1)));
        assertNotNull(batch);
        assertEquals(SECTION_BLOCK_COUNT, batch.count());
        batch.getAll((x, y, z, b) -> {
            final Vec expected = Vec.SECTION.neg();
            if (expected.samePoint(x, y, z)) {
                assertEquals(block, b, "Block at (" + x + "," + y + "," + z + ") should be CHEST");
            } else {
                assertEquals(Block.AIR, b, "Block at (" + x + "," + y + "," + z + ") should be AIR");
            }
        });
    }

    @Test
    public void instanceGetBatchOriginUnaligned(Env env) {
        var instance = env.createEmptyInstance();

        Block block = Block.STONE;
        instance.setBlock(0, 0, 0, block);

        final Vec origin = new Vec(4);

        BlockBatch batch = instance.getBlockBatch(origin, cuboid(Vec.ZERO, Vec.ZERO));
        assertNotNull(batch);
        assertEquals(1, batch.count());
        assertEquals(block, batch.getBlock(origin.neg()));
    }

    @Test
    public void instanceGetBatchUnalignedUnloaded(Env env) {
        var instance = env.createEmptyInstance();

        Block block = Block.STONE;
        instance.setBlock(0, 0, 0, block);

        final Vec origin = new Vec(4);

        BlockBatch batch = instance.getBlockBatch(origin, cuboid(Vec.ONE.neg(), Vec.ONE));
        assertNotNull(batch);
        assertEquals(block, batch.getBlock(origin.neg()));
    }

    @Test
    public void instanceGetBatchAlignedOriginUnaligned(Env env) {
        var instance = env.createEmptyInstance();

        Block block = Block.STONE;
        instance.setBlock(Vec.ONE, block);

        final Vec origin = new Vec(4);

        BlockBatch batch = instance.getBlockBatch(origin, cuboid(Vec.ZERO, Vec.SECTION.sub(1)));
        assertNotNull(batch);
        batch.getAll((x, y, z, b) -> {
            final Vec expected = origin.neg().add(Vec.ONE);
            if (expected.samePoint(x, y, z)) {
                assertEquals(block, b, "Block at (" + x + "," + y + "," + z + ") should be CHEST");
            } else {
                assertEquals(Block.AIR, b, "Block at (" + x + "," + y + "," + z + ") should be AIR");
            }
        });
    }

    @Test
    public void batchAlignedLine(Env env) {
        var instance = env.createEmptyInstance();

        final Block block = Block.STONE;
        final Vec origin = new Vec(4);
        final Area.Line line = Area.line(origin, origin.add(Vec.SECTION.add(7)));

        BlockBatch batch = BlockBatch.aligned(builder -> builder.setBlockArea(line, block));
        for (BlockVec point : line) assertEquals(block, batch.getBlock(point));

        Set<BlockVec> points = StreamSupport.stream(line.spliterator(), false).collect(Collectors.toSet());
        batch.getAll((x, y, z, b) -> {
            BlockVec point = new BlockVec(x, y, z);
            if (points.contains(point)) {
                assertEquals(block, b, "Block at " + point + " should be " + block);
            } else {
                assertEquals(Block.AIR, b, "Block at " + point + " should be AIR");
            }
        });

        instance.setBlockBatch(origin, batch);
        for (BlockVec point : line) {
            assertEquals(block, instance.getBlock(point.add(origin)),
                    "Block at " + point.add(origin) + " should be " + block);
        }
    }

    @Test
    public void batchAlignedLineFar(Env env) {
        var instance = env.createEmptyInstance();

        final Block block = Block.STONE;
        final Vec origin = new Vec(50, 50, 0);
        final Area.Line line = Area.line(origin, origin.add(Vec.SECTION.add(25, 16, 18)));

        BlockBatch batch = BlockBatch.aligned(builder -> builder.setBlockArea(line, block));
        for (BlockVec point : line) assertEquals(block, batch.getBlock(point));

        Set<BlockVec> points = StreamSupport.stream(line.spliterator(), false).collect(Collectors.toSet());
        batch.getAll((x, y, z, b) -> {
            BlockVec point = new BlockVec(x, y, z);
            if (points.contains(point)) {
                assertEquals(block, b, "Block at " + point + " should be " + block);
            } else {
                assertEquals(Block.AIR, b, "Block at " + point + " should be AIR");
            }
        });

        instance.setBlockBatch(origin, batch);
        for (BlockVec point : line) {
            assertEquals(block, instance.getBlock(point.add(origin)),
                    "Block at " + point.add(origin) + " should be " + block);
        }
    }

    @Test
    public void batchUnalignedLine(Env env) {
        var instance = env.createEmptyInstance();

        final Block block = Block.STONE;
        final Vec origin = new Vec(4);
        final Area.Line line = Area.line(origin, origin.add(Vec.SECTION.add(7)));

        BlockBatch batch = BlockBatch.unaligned(builder -> builder.setBlockArea(line, block));
        int count = 0;
        for (BlockVec point : line) {
            assertEquals(block, batch.getBlock(point));
            count++;
        }
        assertEquals(count, batch.count());

        Set<BlockVec> points = StreamSupport.stream(line.spliterator(), false).collect(Collectors.toSet());
        batch.getAll((x, y, z, b) -> {
            BlockVec point = new BlockVec(x, y, z);
            if (points.contains(point)) {
                assertEquals(block, b, "Block at " + point + " should be " + block);
            } else {
                assertEquals(Block.AIR, b, "Block at " + point + " should be AIR");
            }
        });

        instance.setBlockBatch(origin, batch);
        for (BlockVec point : line) {
            assertEquals(block, instance.getBlock(point.add(origin)),
                    "Block at " + point.add(origin) + " should be " + block);
        }
    }

    @Test
    public void batchUnalignedLineFar(Env env) {
        var instance = env.createEmptyInstance();

        final Block block = Block.STONE;
        final Vec origin = new Vec(50, 50, 0);
        final Area.Line line = Area.line(origin, origin.add(Vec.SECTION.add(25, 16, 18)));

        BlockBatch batch = BlockBatch.unaligned(builder -> builder.setBlockArea(line, block));
        int count = 0;
        for (BlockVec vec : line) {
            assertEquals(block, batch.getBlock(vec));
            count++;
        }
        assertEquals(count, batch.count());

        Set<BlockVec> points = StreamSupport.stream(line.spliterator(), false).collect(Collectors.toSet());
        batch.getAll((x, y, z, b) -> {
            BlockVec point = new BlockVec(x, y, z);
            if (points.contains(point)) {
                assertEquals(block, b, "Block at " + point + " should be " + block);
            } else {
                assertEquals(Block.AIR, b, "Block at " + point + " should be AIR");
            }
        });

        instance.setBlockBatch(origin, batch);
        for (BlockVec vec : line) {
            assertEquals(block, instance.getBlock(vec.add(origin)),
                    "Block at " + vec.add(origin) + " should be " + block);
        }
    }

    @Test
    public void multiSectionUnalignedComplexOverlap(Env env) {
        var instance = env.createEmptyInstance();

        // Create a batch that spans multiple sections with complex overlap patterns
        BlockBatch batch = BlockBatch.unaligned(builder -> {
            // Create a 3x3x3 cube of sections (48x48x48 blocks) with specific patterns
            for (int sx = 0; sx < 3; sx++) {
                for (int sy = 0; sy < 3; sy++) {
                    for (int sz = 0; sz < 3; sz++) {
                        int baseX = sx * 16;
                        int baseY = sy * 16;
                        int baseZ = sz * 16;

                        // Place blocks at section corners and centers
                        builder.setBlock(baseX, baseY, baseZ, Block.STONE);
                        builder.setBlock(baseX + 15, baseY + 15, baseZ + 15, Block.DIRT);
                        builder.setBlock(baseX + 8, baseY + 8, baseZ + 8, Block.GRASS_BLOCK);

                        // Place unique identifier block based on section coordinates
                        Block sectionBlock = switch (sx + sy + sz) {
                            case 0 -> Block.COAL_BLOCK;
                            case 1 -> Block.IRON_BLOCK;
                            case 2 -> Block.GOLD_BLOCK;
                            case 3 -> Block.DIAMOND_BLOCK;
                            case 4 -> Block.EMERALD_BLOCK;
                            case 5 -> Block.REDSTONE_BLOCK;
                            default -> Block.LAPIS_BLOCK;
                        };
                        builder.setBlock(baseX + 7, baseY + 7, baseZ + 7, sectionBlock);
                    }
                }
            }
        });

        // Place batch at unaligned origin
        instance.setBlockBatch(10, 20, 30, batch);

        // Verify section corners are placed correctly
        assertEquals(Block.STONE, instance.getBlock(10, 20, 30));
        assertEquals(Block.DIRT, instance.getBlock(25, 35, 45));
        assertEquals(Block.STONE, instance.getBlock(26, 36, 46)); // Second section corner
        assertEquals(Block.DIRT, instance.getBlock(41, 51, 61));

        // Verify section center blocks
        assertEquals(Block.GRASS_BLOCK, instance.getBlock(18, 28, 38));
        assertEquals(Block.GRASS_BLOCK, instance.getBlock(34, 44, 54));

        // Verify unique section identifiers
        assertEquals(Block.COAL_BLOCK, instance.getBlock(17, 27, 37)); // Section (0,0,0)
        assertEquals(Block.IRON_BLOCK, instance.getBlock(33, 27, 37)); // Section (1,0,0)
        assertEquals(Block.IRON_BLOCK, instance.getBlock(17, 43, 37)); // Section (0,1,0)
        assertEquals(Block.IRON_BLOCK, instance.getBlock(17, 27, 53)); // Section (0,0,1)
    }

    @Test
    public void multiSectionUnalignedWithNBT(Env env) {
        var instance = env.createEmptyInstance();

        // Create blocks with NBT for multi-section testing
        Block chest1 = Block.CHEST.withNbt(CompoundBinaryTag.builder()
                .putString("id", "minecraft:chest")
                .putString("CustomName", "\"Section 1 Chest\"")
                .build());

        Block chest2 = Block.CHEST.withNbt(CompoundBinaryTag.builder()
                .putString("id", "minecraft:chest")
                .putString("CustomName", "\"Section 2 Chest\"")
                .build());

        Block sign = Block.OAK_SIGN.withNbt(CompoundBinaryTag.builder()
                .putString("id", "minecraft:sign")
                .putString("Text1", "{\"text\":\"Multi-Section Test\"}")
                .build());

        BlockBatch batch = BlockBatch.unaligned(builder -> {
            // Place NBT blocks across section boundaries
            builder.setBlock(0, 0, 0, chest1);      // Section (0,0,0)
            builder.setBlock(16, 0, 0, chest2);     // Section (1,0,0)
            builder.setBlock(8, 16, 8, sign);       // Section (0,1,0)

            // Add regular blocks to ensure palette works correctly with NBT
            builder.setBlock(1, 0, 0, Block.STONE);
            builder.setBlock(17, 0, 0, Block.DIRT);
            builder.setBlock(9, 16, 8, Block.GRASS_BLOCK);
        });

        instance.setBlockBatch(5, 10, 15, batch);

        // Verify NBT blocks are preserved across sections
        assertEquals(chest1, instance.getBlock(5, 10, 15));
        assertEquals(chest2, instance.getBlock(21, 10, 15));
        assertEquals(sign, instance.getBlock(13, 26, 23));

        // Verify regular blocks
        assertEquals(Block.STONE, instance.getBlock(6, 10, 15));
        assertEquals(Block.DIRT, instance.getBlock(22, 10, 15));
        assertEquals(Block.GRASS_BLOCK, instance.getBlock(14, 26, 23));
    }

    @Test
    public void multiSectionUnalignedBoundaryStress(Env env) {
        var instance = env.createEmptyInstance();

        // Create a batch that stresses section boundaries with dense block placement
        BlockBatch batch = BlockBatch.unaligned(builder -> {
            // Place blocks right at section boundaries
            for (int offset = -2; offset <= 2; offset++) {
                // X boundary at 16
                builder.setBlock(16 + offset, 8, 8, Block.STONE);
                // Y boundary at 16
                builder.setBlock(8, 16 + offset, 8, Block.DIRT);
                // Z boundary at 16
                builder.setBlock(8, 8, 16 + offset, Block.GRASS_BLOCK);

                // Corner intersections
                builder.setBlock(16 + offset, 16 + offset, 8, Block.GOLD_BLOCK);
                builder.setBlock(16 + offset, 8, 16 + offset, Block.IRON_BLOCK);
                builder.setBlock(8, 16 + offset, 16 + offset, Block.DIAMOND_BLOCK);

                // Triple intersection
                builder.setBlock(16 + offset, 16 + offset, 16 + offset, Block.EMERALD_BLOCK);
            }
        });

        instance.setBlockBatch(0, 0, 0, batch);

        // Verify boundary blocks are placed correctly
        for (int offset = -2; offset <= 2; offset++) {
            assertEquals(Block.STONE, instance.getBlock(16 + offset, 8, 8));
            assertEquals(Block.DIRT, instance.getBlock(8, 16 + offset, 8));
            assertEquals(Block.GRASS_BLOCK, instance.getBlock(8, 8, 16 + offset));
            assertEquals(Block.GOLD_BLOCK, instance.getBlock(16 + offset, 16 + offset, 8));
            assertEquals(Block.IRON_BLOCK, instance.getBlock(16 + offset, 8, 16 + offset));
            assertEquals(Block.DIAMOND_BLOCK, instance.getBlock(8, 16 + offset, 16 + offset));
            assertEquals(Block.EMERALD_BLOCK, instance.getBlock(16 + offset, 16 + offset, 16 + offset));
        }
    }

    @Test
    public void multiSectionUnalignedSparse(Env env) {
        var instance = env.createEmptyInstance();

        // Create a sparse batch across many sections with few blocks per section
        BlockBatch batch = BlockBatch.unaligned(builder -> {
            // Place single blocks in different sections
            for (int sx = 0; sx < 5; sx++) {
                for (int sy = 0; sy < 3; sy++) {
                    for (int sz = 0; sz < 5; sz++) {
                        int x = sx * 16 + (sx * 3) % 16;  // Vary position within section
                        int y = sy * 16 + (sy * 5) % 16;
                        int z = sz * 16 + (sz * 7) % 16;

                        Block block = switch ((sx + sy + sz) % 4) {
                            case 0 -> Block.STONE;
                            case 1 -> Block.DIRT;
                            case 2 -> Block.GRASS_BLOCK;
                            default -> Block.COBBLESTONE;
                        };

                        builder.setBlock(x, y, z, block);
                    }
                }
            }
        });

        instance.setBlockBatch(7, 11, 13, batch);

        // Verify sparse blocks are placed correctly
        for (int sx = 0; sx < 5; sx++) {
            for (int sy = 0; sy < 3; sy++) {
                for (int sz = 0; sz < 5; sz++) {
                    int x = sx * 16 + (sx * 3) % 16 + 7;
                    int y = sy * 16 + (sy * 5) % 16 + 11;
                    int z = sz * 16 + (sz * 7) % 16 + 13;

                    Block expectedBlock = switch ((sx + sy + sz) % 4) {
                        case 0 -> Block.STONE;
                        case 1 -> Block.DIRT;
                        case 2 -> Block.GRASS_BLOCK;
                        default -> Block.COBBLESTONE;
                    };

                    assertEquals(expectedBlock, instance.getBlock(x, y, z),
                            "Block at (" + x + "," + y + "," + z + ") should be " + expectedBlock);
                }
            }
        }
    }

    @Test
    public void multiSectionUnalignedNegativeCoordinates(Env env) {
        var instance = env.createEmptyInstance();

        // Create batch with blocks in multiple sections
        BlockBatch batch = BlockBatch.unaligned(builder -> {
            // Place blocks in a 2x2x2 arrangement of sections
            for (int sx = 0; sx < 2; sx++) {
                for (int sy = 0; sy < 2; sy++) {
                    for (int sz = 0; sz < 2; sz++) {
                        int baseX = sx * 16;
                        int baseY = sy * 16;
                        int baseZ = sz * 16;

                        builder.setBlock(baseX + 5, baseY + 5, baseZ + 5, Block.STONE);
                        builder.setBlock(baseX + 10, baseY + 10, baseZ + 10, Block.DIRT);
                    }
                }
            }
        });

        // Place at negative coordinates
        instance.setBlockBatch(-50, -30, -40, batch);

        // Verify blocks are placed correctly
        for (int sx = 0; sx < 2; sx++) {
            for (int sy = 0; sy < 2; sy++) {
                for (int sz = 0; sz < 2; sz++) {
                    int globalX = -50 + sx * 16 + 5;
                    int globalY = -30 + sy * 16 + 5;
                    int globalZ = -40 + sz * 16 + 5;

                    assertEquals(Block.STONE, instance.getBlock(globalX, globalY, globalZ));
                    assertEquals(Block.DIRT, instance.getBlock(globalX + 5, globalY + 5, globalZ + 5));
                }
            }
        }
    }

    @Test
    public void multiSectionUnalignedOverwrite(Env env) {
        var instance = env.createEmptyInstance();

        // First set up existing blocks
        for (int x = 0; x < 48; x++) {
            for (int y = 0; y < 32; y++) {
                for (int z = 0; z < 48; z++) {
                    if ((x + y + z) % 3 == 0) {
                        instance.setBlock(x, y, z, Block.BEDROCK);
                    }
                }
            }
        }

        // Create batch that overwrites some existing blocks
        BlockBatch batch = BlockBatch.unaligned(builder -> {
            for (int sx = 0; sx < 3; sx++) {
                for (int sy = 0; sy < 2; sy++) {
                    for (int sz = 0; sz < 3; sz++) {
                        int baseX = sx * 16;
                        int baseY = sy * 16;
                        int baseZ = sz * 16;

                        // Place new blocks that will overwrite some bedrock
                        builder.setBlock(baseX + 3, baseY + 3, baseZ + 3, Block.DIAMOND_BLOCK);
                        builder.setBlock(baseX + 9, baseY + 9, baseZ + 9, Block.GOLD_BLOCK);
                    }
                }
            }
        });

        instance.setBlockBatch(5, 10, 7, batch);

        // Verify new blocks overwrote the bedrock
        for (int sx = 0; sx < 3; sx++) {
            for (int sy = 0; sy < 2; sy++) {
                for (int sz = 0; sz < 3; sz++) {
                    int globalX = 5 + sx * 16 + 3;
                    int globalY = 10 + sy * 16 + 3;
                    int globalZ = 7 + sz * 16 + 3;

                    assertEquals(Block.DIAMOND_BLOCK, instance.getBlock(globalX, globalY, globalZ));
                    assertEquals(Block.GOLD_BLOCK, instance.getBlock(globalX + 6, globalY + 6, globalZ + 6));
                }
            }
        }

        // Verify that non-overwritten bedrock is still there
        assertEquals(Block.AIR, instance.getBlock(5, 10, 7)); // Original position (5+10+7=22, 22%3=1, so no bedrock initially)
        assertEquals(Block.BEDROCK, instance.getBlock(6, 12, 9)); // (6+12+9=27, 27%3=0, so bedrock should be there)
        assertEquals(Block.BEDROCK, instance.getBlock(3, 9, 9)); // (3+9+9=21, 21%3=0, so bedrock should be there)
    }
}
