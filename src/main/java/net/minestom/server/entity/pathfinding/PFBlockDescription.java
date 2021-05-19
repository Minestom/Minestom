package net.minestom.server.entity.pathfinding;

import com.extollit.gaming.ai.path.model.IBlockDescription;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.Blocks;

import java.util.List;

import static net.minestom.server.instance.block.Blocks.*;

public class PFBlockDescription implements IBlockDescription {

    private static final Short2ObjectMap<PFBlockDescription> BLOCK_DESCRIPTION_MAP = new Short2ObjectOpenHashMap<>();

    /**
     * Gets the {@link PFBlockDescription} linked to the block state id.
     * <p>
     * Cache the result if it is not already.
     *
     * @param blockStateId the block state id
     * @return the {@link PFBlockDescription} linked to {@code blockStateId}
     */
    public static PFBlockDescription getBlockDescription(short blockStateId) {
        if (!BLOCK_DESCRIPTION_MAP.containsKey(blockStateId)) {
            synchronized (BLOCK_DESCRIPTION_MAP) {
                final Block block = Block.REGISTRY.fromStateId(blockStateId);
                final PFBlockDescription blockDescription = new PFBlockDescription(block);
                BLOCK_DESCRIPTION_MAP.put(blockStateId, blockDescription);
                return blockDescription;
            }
        }

        return BLOCK_DESCRIPTION_MAP.get(blockStateId);
    }

    private final Block block;

    public PFBlockDescription(Block block) {
        this.block = block;
    }

    @Override
    public boolean isFenceLike() {
        // Return fences, fencegates and walls.
        return block.getName().toUpperCase().contains("FENCE") || block.getName().toUpperCase().endsWith("WALL");
    }

    @Override
    public boolean isClimbable() {
        // Return ladders and vines (including weeping and twisting vines)
        return block == LADDER || block.getName().toUpperCase().contains("VINE");
    }

    @Override
    public boolean isDoor() {
        // Return wooden doors, trapdoors and wooden fence gates.
        if (block == IRON_DOOR || block == IRON_TRAPDOOR) {
            return false;
        } else {
            return (block.getName().toUpperCase().endsWith("DOOR") || block.getName().toUpperCase().endsWith("FENCE_GATE"));
        }
    }

    @Override
    public boolean isImpeding() {
        return block.getData().isSolid();
    }

    @Override
    public boolean isFullyBounded() {
        // Return false for anything that does not have a full hitbox but impedes
        // e.g. Anvils, Lilypads, Ladders, Walls, Fences, EnchantmentTables
        // Fences & Walls
        if (isFenceLike()) {
            return false;
        }
        // Ladders and Vines
        if (isClimbable()) {
            return false;
        }
        // All doors/trapdoors.
        if (block.getName().toUpperCase().endsWith("DOOR")) {
            return false;
        }
        if (block.getName().toUpperCase().startsWith("POTTED")) {
            return false;
        }
        // Skulls & Heads
        if (block.getName().toUpperCase().contains("SKULL") || block.getName().toUpperCase().contains("HEAD")) {
            return false;
        }
        // Carpets
        if (block.getName().toUpperCase().endsWith("CARPET")) {
            return false;
        }
        // Slabs
        if (block.getName().toUpperCase().contains("SLAB")) {
            return false;
        }
        // Beds
        if (block.getName().toUpperCase().endsWith("BED")) {
            return false;
        }
        // Glass Panes
        if (block.getName().toUpperCase().endsWith("PANE")) {
            return false;
        }

        return !List.of(
                CHORUS_FLOWER, CHORUS_PLANT, BAMBOO, BAMBOO_SAPLING, SEA_PICKLE, TURTLE_EGG, SNOW, FLOWER_POT,
                LILY_PAD, ANVIL, CHIPPED_ANVIL, DAMAGED_ANVIL, CAKE, CACTUS, BREWING_STAND, LECTERN, DAYLIGHT_DETECTOR,
                CAMPFIRE, SOUL_CAMPFIRE, ENCHANTING_TABLE, CHEST, ENDER_CHEST, GRINDSTONE, TRAPPED_CHEST, SOUL_SAND,
                SOUL_SOIL, LANTERN, COCOA, CONDUIT, GRASS_PATH, FARMLAND, END_ROD, STONECUTTER, BELL
        ).contains(block);
    }

    @Override
    public boolean isLiquid() {
        return block.getData().isLiquid();
    }

    @Override
    public boolean isIncinerating() {
        return block == Blocks.LAVA || block == Blocks.FIRE || block == Blocks.SOUL_FIRE;
    }
}
