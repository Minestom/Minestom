package net.minestom.server.instance;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class BlockBatchIntegrationTest {

    @Test
    public void basicExplicit(Env env) {
        var instance = env.createEmptyInstance();
        BlockBatch batch = BlockBatch.explicit(builder -> builder.setBlock(0, 0, 0, Block.STONE));
        instance.setBlockBatch(Vec.ONE, batch);
        assertEquals(Block.STONE, instance.getBlock(1, 1, 1));
        assertEquals(Block.AIR, instance.getBlock(0, 0, 0));
    }

    @Test
    public void basicAligned(Env env) {
        var instance = env.createEmptyInstance();
        BlockBatch batch = BlockBatch.sectionAligned(builder -> builder.setBlock(0, 0, 0, Block.STONE));
        instance.setBlockBatch(Vec.ONE, batch);
        assertEquals(Block.STONE, instance.getBlock(1, 1, 1));
        assertEquals(Block.AIR, instance.getBlock(0, 0, 0));
    }

    @Test
    public void multipleBlocksExplicit(Env env) {
        var instance = env.createEmptyInstance();
        BlockBatch batch = BlockBatch.explicit(builder -> {
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
        BlockBatch batch = BlockBatch.sectionAligned(builder -> {
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
        BlockBatch batch = BlockBatch.sectionAligned(builder -> {
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
        BlockBatch batch = BlockBatch.explicit(builder -> {
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
        BlockBatch batch = BlockBatch.explicit(builder -> {
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

        BlockBatch batch = BlockBatch.explicit(builder -> {
            builder.setBlock(0, 0, 0, chestWithItems);
            builder.setBlock(1, 0, 0, signWithText);
            builder.setBlock(2, 0, 0, Block.STONE); // Regular block
        });

        instance.setBlockBatch(50, 50, 50, batch);

        // Verify blocks with NBT are preserved
        Block placedChest = instance.getBlock(50, 50, 50);
        Block placedSign = instance.getBlock(51, 50, 50);
        Block placedStone = instance.getBlock(52, 50, 50);

        assertEquals(Block.CHEST, placedChest.withNbt(null));
        assertEquals(Block.OAK_SIGN, placedSign.withNbt(null));
        assertEquals(Block.STONE, placedStone);

        // NBT should be preserved for complex blocks
        assertNotNull(placedChest.nbt());
        assertNotNull(placedSign.nbt());
        assertNull(placedStone.nbt());
    }

    @Test
    public void emptyBatch(Env env) {
        var instance = env.createEmptyInstance();

        // Set some initial blocks
        instance.setBlock(10, 10, 10, Block.STONE);

        // Apply empty batch
        BlockBatch emptyBatch = BlockBatch.explicit(builder -> {
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
        BlockBatch batch = BlockBatch.explicit(builder -> {
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

        BlockBatch batch = BlockBatch.explicit(builder -> {
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
        BlockBatch explicitBatch = BlockBatch.explicit(builder -> {
            for (int x = 0; x < 4; x++) {
                for (int y = 0; y < 4; y++) {
                    for (int z = 0; z < 4; z++) {
                        builder.setBlock(x, y, z, Block.STONE);
                    }
                }
            }
        });

        BlockBatch alignedBatch = BlockBatch.sectionAligned(builder -> {
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
}
