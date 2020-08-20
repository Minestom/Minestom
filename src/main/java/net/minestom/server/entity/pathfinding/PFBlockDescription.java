package net.minestom.server.entity.pathfinding;

import com.extollit.gaming.ai.path.model.IBlockDescription;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minestom.server.instance.block.Block;

public class PFBlockDescription implements IBlockDescription {

    private static final Short2ObjectMap<PFBlockDescription> BLOCK_DESCRITION_MAP = new Short2ObjectOpenHashMap<>();

    /**
     * Get the {@link PFBlockDescription} linked to the block state id
     * <p>
     * Cache the result if it is not already
     *
     * @param blockStateId the block state id
     * @return the {@link PFBlockDescription} linked to {@code blockStateId}
     */
    public static PFBlockDescription getBlockDescription(short blockStateId) {
        if (!BLOCK_DESCRITION_MAP.containsKey(blockStateId)) {
            synchronized (BLOCK_DESCRITION_MAP) {
                final Block block = Block.fromStateId(blockStateId);
                final PFBlockDescription blockDescription = new PFBlockDescription(block);
                BLOCK_DESCRITION_MAP.put(blockStateId, blockDescription);
                return blockDescription;
            }
        }

        return BLOCK_DESCRITION_MAP.get(blockStateId);
    }

    private final Block block;

    public PFBlockDescription(Block block) {
        this.block = block;
    }

    @Override
    public boolean isFenceLike() {
        // Return fences, fencegates and walls.
        return block.name().toUpperCase().contains("FENCE") || block.name().toUpperCase().endsWith("WALL");
    }

    @Override
    public boolean isClimbable() {
        // Return ladders and vines (including weeping and twisting vines)
        return block == Block.LADDER || block.name().toUpperCase().contains("VINE");
    }

    @Override
    public boolean isDoor() {
        // Return wooden doors, trapdoors and wooden fence gates.
        if (block == Block.IRON_DOOR || block == Block.IRON_TRAPDOOR) {
            return false;
        } else {
            return (block.name().toUpperCase().endsWith("DOOR") || block.name().toUpperCase().endsWith("FENCE_GATE"));
        }
    }

    @Override
    public boolean isImpeding() {
        return block.isSolid();
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
        if (block.name().toUpperCase().endsWith("DOOR")) {
            return false;
        }
        if (block.name().toUpperCase().startsWith("POTTED")) {
            return false;
        }
        // Skulls & Heads
        if (block.name().toUpperCase().contains("SKULL") || block.name().toUpperCase().contains("HEAD")) {
            return false;
        }
        // Carpets
        if (block.name().toUpperCase().endsWith("CARPET")) {
            return false;
        }
        // Slabs
        if (block.name().toUpperCase().contains("SLAB")) {
            return false;
        }
        // Beds
        if (block.name().toUpperCase().endsWith("BED")) {
            return false;
        }
        // Glass Panes
        if (block.name().toUpperCase().endsWith("PANE")) {
            return false;
        }

        switch (block) {
            case CHORUS_FLOWER:
            case CHORUS_PLANT:
            case BAMBOO:
            case BAMBOO_SAPLING:
            case SEA_PICKLE:
            case TURTLE_EGG:
            case SNOW:
            case FLOWER_POT:
            case LILY_PAD:
            case ANVIL:
            case CHIPPED_ANVIL:
            case DAMAGED_ANVIL:
            case CAKE:
            case CACTUS:
            case BREWING_STAND:
            case LECTERN:
            case DAYLIGHT_DETECTOR:
            case CAMPFIRE:
            case SOUL_CAMPFIRE:
            case ENCHANTING_TABLE:
            case CHEST:
            case ENDER_CHEST:
            case GRINDSTONE:
            case TRAPPED_CHEST:
            case SOUL_SAND:
            case SOUL_SOIL:
            case LANTERN:
            case COCOA:
            case CONDUIT:
            case GRASS_PATH:
            case FARMLAND:
            case END_ROD:
            case STONECUTTER:
            case BELL: {
                return false;
            }
            default: {
                return true;
            }
        }
    }

    @Override
    public boolean isLiquid() {
        return block.isLiquid();
    }

    @Override
    public boolean isIncinerating() {
        return block == Block.LAVA || block == Block.FIRE || block == Block.SOUL_FIRE;
    }
}
