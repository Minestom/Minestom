package net.minestom.server.extras;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.rule.vanilla.AxisPlacementRule;
import net.minestom.server.instance.block.rule.vanilla.RedstonePlacementRule;
import net.minestom.server.instance.block.rule.vanilla.StairsPlacementRule;
import net.minestom.server.instance.block.rule.vanilla.WallPlacementRule;

public final class PlacementRules {

    /**
     * Register all the block placement rules supported:
     * - axis blocks (logs, hay block, etc)
     * - stairs
     * - walls
     * - redstone dust
     */
    public static void init() {
        initAxisBlocks();
        initStairs();
        initWalls();
        initRedstone();
    }

    /**
     * Register all the blocks that support axis placement (logs, hay block, etc)
     * using {@link AxisPlacementRule}.
     */
    public static void initAxisBlocks() {
        BlockManager blockManager = MinecraftServer.getBlockManager();
        for (Block block : Block.values()) {
            if (block.getProperty("axis") != null) {
                blockManager.registerBlockPlacementRule(new AxisPlacementRule(block));
            }
        }
    }

    /**
     * Register all the stairs blocks using {@link StairsPlacementRule}.
     */
    public static void initStairs() {
        BlockManager blockManager = MinecraftServer.getBlockManager();
        for (Block block : Block.values()) {
            if (block.namespace().value().endsWith("stairs")) {
                blockManager.registerBlockPlacementRule(new StairsPlacementRule(block));
            }
        }
    }

    /**
     * Register all the wall blocks using {@link WallPlacementRule}.
     */
    public static void initWalls() {
        BlockManager blockManager = MinecraftServer.getBlockManager();
        for (Block block : Block.values()) {
            String namespace = block.namespace().value();
            if (namespace.endsWith("wall")) {
                blockManager.registerBlockPlacementRule(new WallPlacementRule(block));
            }
        }
    }

    /**
     * Register the {@link RedstonePlacementRule}.
     */
    public static void initRedstone() {
        BlockManager blockManager = MinecraftServer.getBlockManager();
        blockManager.registerBlockPlacementRule(new RedstonePlacementRule());
    }
}
