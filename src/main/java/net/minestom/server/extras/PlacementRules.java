package net.minestom.server.extras;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.rule.vanilla.AxisPlacementRule;
import net.minestom.server.instance.block.rule.vanilla.RedstonePlacementRule;
import net.minestom.server.instance.block.rule.vanilla.StairsPlacementRule;
import net.minestom.server.instance.block.rule.vanilla.WallPlacementRule;

public final class PlacementRules {

    public static void init() {
        initAxisBlocks();
        initStairs();
        initWalls();
        initRedstone();
    }

    public static void initAxisBlocks() {
        BlockManager blockManager = MinecraftServer.getBlockManager();
        for (Block block : Block.values()) {
            if (block.getProperty("axis") != null) {
                blockManager.registerBlockPlacementRule(new AxisPlacementRule(block));
            }
        }
    }

    public static void initStairs() {
        BlockManager blockManager = MinecraftServer.getBlockManager();
        for (Block block : Block.values()) {
            if (block.namespace().value().endsWith("stairs")) {
                blockManager.registerBlockPlacementRule(new StairsPlacementRule(block));
            }
        }
    }

    public static void initWalls() {
        BlockManager blockManager = MinecraftServer.getBlockManager();
        for (Block block : Block.values()) {
            String namespace = block.namespace().value();
            if (namespace.endsWith("wall") || namespace.endsWith("fence")) {
                blockManager.registerBlockPlacementRule(new WallPlacementRule(block));
            }
        }
    }

    public static void initRedstone() {
        BlockManager blockManager = MinecraftServer.getBlockManager();
        blockManager.registerBlockPlacementRule(new RedstonePlacementRule());
    }
}
