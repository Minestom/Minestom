package net.minestom.server.extras.vanilla;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.extras.vanilla.blocks.DAxisBlock;
import net.minestom.server.extras.vanilla.blocks.DChestBlock;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.utils.BlockPosition;
import org.jetbrains.annotations.NotNull;

public final class VanillaBlocks {

    private static final BlockManager BLOCK_MANAGER = MinecraftServer.getBlockManager();

    private static final CustomBlock[] BLOCKS = new CustomBlock[Short.MAX_VALUE];

    public static final short VANILLA_LAST_ID;

    static {
        short id = 1;
        BLOCKS[Block.BONE_BLOCK.getBlockId()] = new DAxisBlock(Block.BONE_BLOCK, "dv_bone_block", id++);
        BLOCKS[Block.HAY_BLOCK.getBlockId()] = new DAxisBlock(Block.HAY_BLOCK, "dv_hay_block", id++);
        BLOCKS[Block.OAK_LOG.getBlockId()] = new DAxisBlock(Block.OAK_LOG, "dv_oak_log", id++);
        BLOCKS[Block.SPRUCE_LOG.getBlockId()] = new DAxisBlock(Block.SPRUCE_LOG, "dv_spruce_log", id++);
        BLOCKS[Block.BIRCH_LOG.getBlockId()] = new DAxisBlock(Block.BIRCH_LOG, "dv_birch_log", id++);
        BLOCKS[Block.JUNGLE_LOG.getBlockId()] = new DAxisBlock(Block.JUNGLE_LOG, "dv_jungle_log", id++);
        BLOCKS[Block.ACACIA_LOG.getBlockId()] = new DAxisBlock(Block.ACACIA_LOG, "dv_acacia_log", id++);
        BLOCKS[Block.DARK_OAK_LOG.getBlockId()] = new DAxisBlock(Block.DARK_OAK_LOG, "dv_dark_oak_log", id++);
        BLOCKS[Block.STRIPPED_OAK_LOG.getBlockId()] = new DAxisBlock(Block.STRIPPED_OAK_LOG, "dv_stripped_oak_log", id++);
        BLOCKS[Block.STRIPPED_SPRUCE_LOG.getBlockId()] = new DAxisBlock(Block.STRIPPED_SPRUCE_LOG, "dv_stripped_spruce_log",  id++);
        BLOCKS[Block.STRIPPED_BIRCH_LOG.getBlockId()] = new DAxisBlock(Block.STRIPPED_BIRCH_LOG, "dv_stripped_birch_log",  id++);
        BLOCKS[Block.STRIPPED_JUNGLE_LOG.getBlockId()] = new DAxisBlock(Block.STRIPPED_JUNGLE_LOG, "dv_stripped_jungle_log",  id++);
        BLOCKS[Block.STRIPPED_ACACIA_LOG.getBlockId()] = new DAxisBlock(Block.STRIPPED_ACACIA_LOG, "dv_stripped_acacia_log",  id++);
        BLOCKS[Block.STRIPPED_DARK_OAK_LOG.getBlockId()] = new DAxisBlock(Block.STRIPPED_DARK_OAK_LOG, "dv_stripped_dark_oak_log",  id++);
        BLOCKS[Block.PURPUR_PILLAR.getBlockId()] = new DAxisBlock(Block.PURPUR_PILLAR, "dv_purpur_pillar",  id++);
        BLOCKS[Block.QUARTZ_PILLAR.getBlockId()] = new DAxisBlock(Block.QUARTZ_PILLAR, "dv_quartz_pillar",  id++);
        BLOCKS[Block.OAK_WOOD.getBlockId()] = new DAxisBlock(Block.OAK_WOOD, "dv_oak_wood",  id++);
        BLOCKS[Block.SPRUCE_WOOD.getBlockId()] = new DAxisBlock(Block.SPRUCE_WOOD, "dv_spruce_wood",  id++);
        BLOCKS[Block.BIRCH_WOOD.getBlockId()] = new DAxisBlock(Block.BIRCH_WOOD, "dv_birch_wood",  id++);
        BLOCKS[Block.JUNGLE_WOOD.getBlockId()] = new DAxisBlock(Block.JUNGLE_WOOD, "dv_jungle_wood",  id++);
        BLOCKS[Block.ACACIA_WOOD.getBlockId()] = new DAxisBlock(Block.ACACIA_WOOD, "dv_acacia_wood",  id++);
        BLOCKS[Block.DARK_OAK_WOOD.getBlockId()] = new DAxisBlock(Block.DARK_OAK_WOOD, "dv_dark_oak_wood",  id++);
        BLOCKS[Block.STRIPPED_OAK_WOOD.getBlockId()] = new DAxisBlock(Block.STRIPPED_OAK_WOOD, "dv_stripped_oak_wood",  id++);
        BLOCKS[Block.STRIPPED_SPRUCE_WOOD.getBlockId()] = new DAxisBlock(Block.STRIPPED_SPRUCE_WOOD, "dv_stripped_spruce_wood",  id++);
        BLOCKS[Block.STRIPPED_BIRCH_WOOD.getBlockId()] = new DAxisBlock(Block.STRIPPED_BIRCH_WOOD, "dv_stripped_birch_wood",  id++);
        BLOCKS[Block.STRIPPED_JUNGLE_WOOD.getBlockId()] = new DAxisBlock(Block.STRIPPED_JUNGLE_WOOD, "dv_stripped_jungle_wood",  id++);
        BLOCKS[Block.STRIPPED_ACACIA_WOOD.getBlockId()] = new DAxisBlock(Block.STRIPPED_ACACIA_WOOD, "dv_stripped_acacia_wood",  id++);
        BLOCKS[Block.STRIPPED_DARK_OAK_WOOD.getBlockId()] = new DAxisBlock(Block.STRIPPED_DARK_OAK_WOOD, "dv_stripped_dark_oak_wood",  id++);
        BLOCKS[Block.CRIMSON_STEM.getBlockId()] = new DAxisBlock(Block.CRIMSON_STEM, "dv_crimson_stem",  id++);
        BLOCKS[Block.WARPED_STEM.getBlockId()] = new DAxisBlock(Block.WARPED_STEM, "dv_warped_stem",  id++);
        BLOCKS[Block.STRIPPED_WARPED_STEM.getBlockId()] = new DAxisBlock(Block.STRIPPED_WARPED_STEM, "dv_stripped_warped_stem",  id++);
        BLOCKS[Block.STRIPPED_CRIMSON_STEM.getBlockId()] = new DAxisBlock(Block.STRIPPED_CRIMSON_STEM, "dv_stripped_crimson_stem",  id++);

        BLOCKS[Block.CHEST.getBlockId()] = new DChestBlock(Block.CHEST, "dv_chest",  id++);
        BLOCKS[Block.TRAPPED_CHEST.getBlockId()] = new DChestBlock(Block.TRAPPED_CHEST, "dv_trapped_chest",  id++);

        VANILLA_LAST_ID = (short) (id - 1);

        for (int i = 0; i < BLOCKS.length; i++) if (BLOCKS[i] != null) BLOCK_MANAGER.registerCustomBlock(BLOCKS[i]);
    }

    public static void registerBlocksInstance(Instance instance) {
        instance.addEventCallback(PlayerBlockPlaceEvent.class, event -> {
            CustomBlock customBlock = BLOCKS[event.getBlockStateId()];
            if (customBlock == null) return;

            event.setCustomBlock(customBlock.getCustomBlockId());
        });
    }

    public static void registerBlock(short blockId, CustomBlock customBlock) {
        BLOCKS[blockId] = customBlock;
    }

    public static void sendBlockChange(@NotNull Chunk chunk, @NotNull BlockPosition blockPosition, short blockStateId) { // Probs not best place to put this.
        BlockChangePacket blockChangePacket = new BlockChangePacket();
        blockChangePacket.blockPosition = blockPosition;
        blockChangePacket.blockStateId = blockStateId;
        chunk.sendPacketToViewers(blockChangePacket);
    }
}
