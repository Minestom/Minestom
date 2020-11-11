package net.minestom.server.extras;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.rule.vanilla.AxisPlacementRule;
import net.minestom.server.instance.block.rule.vanilla.RedstonePlacementRule;
import net.minestom.server.instance.block.rule.vanilla.WallPlacementRule;

public final class PlacementRules {

	public static void init() {
		BlockManager blockManager = MinecraftServer.getBlockManager();
		blockManager.registerBlockPlacementRule(new RedstonePlacementRule());
		blockManager.registerBlockPlacementRule(new AxisPlacementRule(Block.BONE_BLOCK));
		blockManager.registerBlockPlacementRule(new AxisPlacementRule(Block.HAY_BLOCK));
		blockManager.registerBlockPlacementRule(new AxisPlacementRule(Block.OAK_LOG));
		blockManager.registerBlockPlacementRule(new AxisPlacementRule(Block.SPRUCE_LOG));
		blockManager.registerBlockPlacementRule(new AxisPlacementRule(Block.BIRCH_LOG));
		blockManager.registerBlockPlacementRule(new AxisPlacementRule(Block.JUNGLE_LOG));
		blockManager.registerBlockPlacementRule(new AxisPlacementRule(Block.ACACIA_LOG));
		blockManager.registerBlockPlacementRule(new AxisPlacementRule(Block.DARK_OAK_LOG));
		blockManager.registerBlockPlacementRule(new AxisPlacementRule(Block.CRIMSON_STEM));
		blockManager.registerBlockPlacementRule(new AxisPlacementRule(Block.WARPED_STEM));
		blockManager.registerBlockPlacementRule(new AxisPlacementRule(Block.STRIPPED_OAK_LOG));
		blockManager.registerBlockPlacementRule(new AxisPlacementRule(Block.STRIPPED_SPRUCE_LOG));
		blockManager.registerBlockPlacementRule(new AxisPlacementRule(Block.STRIPPED_BIRCH_LOG));
		blockManager.registerBlockPlacementRule(new AxisPlacementRule(Block.STRIPPED_JUNGLE_LOG));
		blockManager.registerBlockPlacementRule(new AxisPlacementRule(Block.STRIPPED_ACACIA_LOG));
		blockManager.registerBlockPlacementRule(new AxisPlacementRule(Block.STRIPPED_DARK_OAK_LOG));
		blockManager.registerBlockPlacementRule(new AxisPlacementRule(Block.STRIPPED_CRIMSON_STEM));
		blockManager.registerBlockPlacementRule(new AxisPlacementRule(Block.STRIPPED_WARPED_STEM));
		blockManager.registerBlockPlacementRule(new AxisPlacementRule(Block.PURPUR_PILLAR));
		blockManager.registerBlockPlacementRule(new AxisPlacementRule(Block.QUARTZ_PILLAR));
		blockManager.registerBlockPlacementRule(new AxisPlacementRule(Block.OAK_WOOD));
		blockManager.registerBlockPlacementRule(new AxisPlacementRule(Block.SPRUCE_WOOD));
		blockManager.registerBlockPlacementRule(new AxisPlacementRule(Block.BIRCH_WOOD));
		blockManager.registerBlockPlacementRule(new AxisPlacementRule(Block.JUNGLE_WOOD));
		blockManager.registerBlockPlacementRule(new AxisPlacementRule(Block.ACACIA_WOOD));
		blockManager.registerBlockPlacementRule(new AxisPlacementRule(Block.DARK_OAK_WOOD));
		blockManager.registerBlockPlacementRule(new AxisPlacementRule(Block.CRIMSON_STEM));
		blockManager.registerBlockPlacementRule(new AxisPlacementRule(Block.WARPED_STEM));
		blockManager.registerBlockPlacementRule(new AxisPlacementRule(Block.STRIPPED_OAK_WOOD));
		blockManager.registerBlockPlacementRule(new AxisPlacementRule(Block.STRIPPED_SPRUCE_WOOD));
		blockManager.registerBlockPlacementRule(new AxisPlacementRule(Block.STRIPPED_BIRCH_WOOD));
		blockManager.registerBlockPlacementRule(new AxisPlacementRule(Block.STRIPPED_JUNGLE_WOOD));
		blockManager.registerBlockPlacementRule(new AxisPlacementRule(Block.STRIPPED_ACACIA_WOOD));
		blockManager.registerBlockPlacementRule(new AxisPlacementRule(Block.STRIPPED_DARK_OAK_WOOD));
		blockManager.registerBlockPlacementRule(new AxisPlacementRule(Block.STRIPPED_CRIMSON_STEM));
		blockManager.registerBlockPlacementRule(new AxisPlacementRule(Block.STRIPPED_WARPED_STEM));
		blockManager.registerBlockPlacementRule(new WallPlacementRule(Block.COBBLESTONE_WALL));
		blockManager.registerBlockPlacementRule(new WallPlacementRule(Block.MOSSY_COBBLESTONE_WALL));
	}
}
