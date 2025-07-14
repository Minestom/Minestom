package net.minestom.server.instance;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.coordinate.Area;
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

        Area.Line line = Area.line(origin, origin.add(Vec.SECTION.add(7)));

        BlockBatch batch = BlockBatch.aligned(builder -> builder.setBlockArea(line, block));
        for (Vec point : line) assertEquals(block, batch.getBlock(point));

        Set<Vec> points = StreamSupport.stream(line.spliterator(), false).collect(Collectors.toSet());
        batch.getAll((x, y, z, b) -> {
            Vec point = new Vec(x, y, z);
            if (points.contains(point)) {
                assertEquals(block, b, "Block at " + point + " should be " + block);
            } else {
                assertEquals(Block.AIR, b, "Block at " + point + " should be AIR");
            }
        });

        instance.setBlockBatch(origin, batch);
        for (Vec point : line) {
            assertEquals(block, instance.getBlock(point.add(origin)),
                    "Block at " + point.add(origin) + " should be " + block);
        }
    }

    @Test
    public void batchUnalignedLine(Env env) {
        var instance = env.createEmptyInstance();

        final Block block = Block.STONE;
        final Vec origin = new Vec(4);

        Area.Line line = Area.line(origin, origin.add(Vec.SECTION.add(7)));

        BlockBatch batch = BlockBatch.unaligned(builder -> builder.setBlockArea(line, block));
        int count = 0;
        for (Vec point : line) {
            assertEquals(block, batch.getBlock(point));
            count++;
        }
        assertEquals(count, batch.count());

        Set<Vec> points = StreamSupport.stream(line.spliterator(), false).collect(Collectors.toSet());
        batch.getAll((x, y, z, b) -> {
            Vec point = new Vec(x, y, z);
            if (points.contains(point)) {
                assertEquals(block, b, "Block at " + point + " should be " + block);
            } else {
                assertEquals(Block.AIR, b, "Block at " + point + " should be AIR");
            }
        });

        instance.setBlockBatch(origin, batch);
        for (Vec point : line) {
            assertEquals(block, instance.getBlock(point.add(origin)),
                    "Block at " + point.add(origin) + " should be " + block);
        }
    }
}
