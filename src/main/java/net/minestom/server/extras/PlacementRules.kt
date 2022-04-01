package net.minestom.server.extras

import net.minestom.server.utils.validate.Check.stateCondition
import net.minestom.server.MinecraftServer.Companion.process
import net.minestom.server.ServerProcess.isAlive
import net.minestom.server.extras.mojangAuth.MojangCrypt.generateKeyPair
import net.minestom.server.MinecraftServer.Companion.blockManager
import net.minestom.server.extras.MojangAuth
import net.minestom.server.MinecraftServer
import net.minestom.server.extras.mojangAuth.MojangCrypt
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.block.BlockManager
import net.minestom.server.instance.block.rule.vanilla.RedstonePlacementRule
import net.minestom.server.instance.block.rule.vanilla.AxisPlacementRule
import net.minestom.server.instance.block.rule.vanilla.WallPlacementRule

object PlacementRules {
    fun init() {
        val blockManager = blockManager
        blockManager.registerBlockPlacementRule(RedstonePlacementRule())
        blockManager.registerBlockPlacementRule(AxisPlacementRule(Block.BONE_BLOCK))
        blockManager.registerBlockPlacementRule(AxisPlacementRule(Block.HAY_BLOCK))
        blockManager.registerBlockPlacementRule(AxisPlacementRule(Block.OAK_LOG))
        blockManager.registerBlockPlacementRule(AxisPlacementRule(Block.SPRUCE_LOG))
        blockManager.registerBlockPlacementRule(AxisPlacementRule(Block.BIRCH_LOG))
        blockManager.registerBlockPlacementRule(AxisPlacementRule(Block.JUNGLE_LOG))
        blockManager.registerBlockPlacementRule(AxisPlacementRule(Block.ACACIA_LOG))
        blockManager.registerBlockPlacementRule(AxisPlacementRule(Block.DARK_OAK_LOG))
        blockManager.registerBlockPlacementRule(AxisPlacementRule(Block.CRIMSON_STEM))
        blockManager.registerBlockPlacementRule(AxisPlacementRule(Block.WARPED_STEM))
        blockManager.registerBlockPlacementRule(AxisPlacementRule(Block.STRIPPED_OAK_LOG))
        blockManager.registerBlockPlacementRule(AxisPlacementRule(Block.STRIPPED_SPRUCE_LOG))
        blockManager.registerBlockPlacementRule(AxisPlacementRule(Block.STRIPPED_BIRCH_LOG))
        blockManager.registerBlockPlacementRule(AxisPlacementRule(Block.STRIPPED_JUNGLE_LOG))
        blockManager.registerBlockPlacementRule(AxisPlacementRule(Block.STRIPPED_ACACIA_LOG))
        blockManager.registerBlockPlacementRule(AxisPlacementRule(Block.STRIPPED_DARK_OAK_LOG))
        blockManager.registerBlockPlacementRule(AxisPlacementRule(Block.STRIPPED_CRIMSON_STEM))
        blockManager.registerBlockPlacementRule(AxisPlacementRule(Block.STRIPPED_WARPED_STEM))
        blockManager.registerBlockPlacementRule(AxisPlacementRule(Block.PURPUR_PILLAR))
        blockManager.registerBlockPlacementRule(AxisPlacementRule(Block.QUARTZ_PILLAR))
        blockManager.registerBlockPlacementRule(AxisPlacementRule(Block.OAK_WOOD))
        blockManager.registerBlockPlacementRule(AxisPlacementRule(Block.SPRUCE_WOOD))
        blockManager.registerBlockPlacementRule(AxisPlacementRule(Block.BIRCH_WOOD))
        blockManager.registerBlockPlacementRule(AxisPlacementRule(Block.JUNGLE_WOOD))
        blockManager.registerBlockPlacementRule(AxisPlacementRule(Block.ACACIA_WOOD))
        blockManager.registerBlockPlacementRule(AxisPlacementRule(Block.DARK_OAK_WOOD))
        blockManager.registerBlockPlacementRule(AxisPlacementRule(Block.CRIMSON_STEM))
        blockManager.registerBlockPlacementRule(AxisPlacementRule(Block.WARPED_STEM))
        blockManager.registerBlockPlacementRule(AxisPlacementRule(Block.STRIPPED_OAK_WOOD))
        blockManager.registerBlockPlacementRule(AxisPlacementRule(Block.STRIPPED_SPRUCE_WOOD))
        blockManager.registerBlockPlacementRule(AxisPlacementRule(Block.STRIPPED_BIRCH_WOOD))
        blockManager.registerBlockPlacementRule(AxisPlacementRule(Block.STRIPPED_JUNGLE_WOOD))
        blockManager.registerBlockPlacementRule(AxisPlacementRule(Block.STRIPPED_ACACIA_WOOD))
        blockManager.registerBlockPlacementRule(AxisPlacementRule(Block.STRIPPED_DARK_OAK_WOOD))
        blockManager.registerBlockPlacementRule(AxisPlacementRule(Block.STRIPPED_CRIMSON_STEM))
        blockManager.registerBlockPlacementRule(AxisPlacementRule(Block.STRIPPED_WARPED_STEM))
        blockManager.registerBlockPlacementRule(WallPlacementRule(Block.COBBLESTONE_WALL))
        blockManager.registerBlockPlacementRule(WallPlacementRule(Block.MOSSY_COBBLESTONE_WALL))
    }
}