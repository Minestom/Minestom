package net.minestom.server.extras;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.Blocks;
import net.minestom.server.instance.block.rule.vanilla.AxisPlacementRule;
import net.minestom.server.instance.block.rule.vanilla.RedstonePlacementRule;
import net.minestom.server.instance.block.rule.vanilla.WallPlacementRule;

public final class PlacementRules {

    public static void init() {
        BlockManager blockManager = MinecraftServer.getBlockManager();
        blockManager.registerBlockPlacementRule(new RedstonePlacementRule());
        blockManager.registerBlockPlacementRule(new AxisPlacementRule(Blocks.BONE_BLOCK));
        blockManager.registerBlockPlacementRule(new AxisPlacementRule(Blocks.HAY_BLOCK));
        blockManager.registerBlockPlacementRule(new AxisPlacementRule(Blocks.OAK_LOG));
        blockManager.registerBlockPlacementRule(new AxisPlacementRule(Blocks.SPRUCE_LOG));
        blockManager.registerBlockPlacementRule(new AxisPlacementRule(Blocks.BIRCH_LOG));
        blockManager.registerBlockPlacementRule(new AxisPlacementRule(Blocks.JUNGLE_LOG));
        blockManager.registerBlockPlacementRule(new AxisPlacementRule(Blocks.ACACIA_LOG));
        blockManager.registerBlockPlacementRule(new AxisPlacementRule(Blocks.DARK_OAK_LOG));
        blockManager.registerBlockPlacementRule(new AxisPlacementRule(Blocks.CRIMSON_STEM));
        blockManager.registerBlockPlacementRule(new AxisPlacementRule(Blocks.WARPED_STEM));
        blockManager.registerBlockPlacementRule(new AxisPlacementRule(Blocks.STRIPPED_OAK_LOG));
        blockManager.registerBlockPlacementRule(new AxisPlacementRule(Blocks.STRIPPED_SPRUCE_LOG));
        blockManager.registerBlockPlacementRule(new AxisPlacementRule(Blocks.STRIPPED_BIRCH_LOG));
        blockManager.registerBlockPlacementRule(new AxisPlacementRule(Blocks.STRIPPED_JUNGLE_LOG));
        blockManager.registerBlockPlacementRule(new AxisPlacementRule(Blocks.STRIPPED_ACACIA_LOG));
        blockManager.registerBlockPlacementRule(new AxisPlacementRule(Blocks.STRIPPED_DARK_OAK_LOG));
        blockManager.registerBlockPlacementRule(new AxisPlacementRule(Blocks.STRIPPED_CRIMSON_STEM));
        blockManager.registerBlockPlacementRule(new AxisPlacementRule(Blocks.STRIPPED_WARPED_STEM));
        blockManager.registerBlockPlacementRule(new AxisPlacementRule(Blocks.PURPUR_PILLAR));
        blockManager.registerBlockPlacementRule(new AxisPlacementRule(Blocks.QUARTZ_PILLAR));
        blockManager.registerBlockPlacementRule(new AxisPlacementRule(Blocks.OAK_WOOD));
        blockManager.registerBlockPlacementRule(new AxisPlacementRule(Blocks.SPRUCE_WOOD));
        blockManager.registerBlockPlacementRule(new AxisPlacementRule(Blocks.BIRCH_WOOD));
        blockManager.registerBlockPlacementRule(new AxisPlacementRule(Blocks.JUNGLE_WOOD));
        blockManager.registerBlockPlacementRule(new AxisPlacementRule(Blocks.ACACIA_WOOD));
        blockManager.registerBlockPlacementRule(new AxisPlacementRule(Blocks.DARK_OAK_WOOD));
        blockManager.registerBlockPlacementRule(new AxisPlacementRule(Blocks.CRIMSON_STEM));
        blockManager.registerBlockPlacementRule(new AxisPlacementRule(Blocks.WARPED_STEM));
        blockManager.registerBlockPlacementRule(new AxisPlacementRule(Blocks.STRIPPED_OAK_WOOD));
        blockManager.registerBlockPlacementRule(new AxisPlacementRule(Blocks.STRIPPED_SPRUCE_WOOD));
        blockManager.registerBlockPlacementRule(new AxisPlacementRule(Blocks.STRIPPED_BIRCH_WOOD));
        blockManager.registerBlockPlacementRule(new AxisPlacementRule(Blocks.STRIPPED_JUNGLE_WOOD));
        blockManager.registerBlockPlacementRule(new AxisPlacementRule(Blocks.STRIPPED_ACACIA_WOOD));
        blockManager.registerBlockPlacementRule(new AxisPlacementRule(Blocks.STRIPPED_DARK_OAK_WOOD));
        blockManager.registerBlockPlacementRule(new AxisPlacementRule(Blocks.STRIPPED_CRIMSON_STEM));
        blockManager.registerBlockPlacementRule(new AxisPlacementRule(Blocks.STRIPPED_WARPED_STEM));
        blockManager.registerBlockPlacementRule(new WallPlacementRule(Blocks.COBBLESTONE_WALL));
        blockManager.registerBlockPlacementRule(new WallPlacementRule(Blocks.MOSSY_COBBLESTONE_WALL));
    }
}
