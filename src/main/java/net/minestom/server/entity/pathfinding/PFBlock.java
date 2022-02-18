package net.minestom.server.entity.pathfinding;

import com.extollit.gaming.ai.path.model.IBlockDescription;
import com.extollit.gaming.ai.path.model.IBlockObject;
import com.extollit.linalg.immutable.AxisAlignedBBox;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import space.vectrix.flare.fastutil.Short2ObjectSyncMap;

@ApiStatus.Internal
public final class PFBlock implements IBlockDescription, IBlockObject {
    private static final Short2ObjectSyncMap<PFBlock> BLOCK_DESCRIPTION_MAP = Short2ObjectSyncMap.hashmap();

    /**
     * Gets the {@link PFBlock} linked to the block state id.
     * <p>
     * Cache the result if it is not already.
     *
     * @param block the block
     * @return the {@link PFBlock} linked to {@code blockStateId}
     */
    public static @NotNull PFBlock get(@NotNull Block block) {
        return BLOCK_DESCRIPTION_MAP.computeIfAbsent(block.stateId(), state -> new PFBlock(block));
    }

    private final Block block;

    PFBlock(Block block) {
        this.block = block;
    }

    @Override
    public AxisAlignedBBox bounds() {
        return new AxisAlignedBBox(
                0, 0, 0,
                1, 1, 1
        );
    }

    @Override
    public boolean isFenceLike() {
        // TODO: Use Hitbox
        // Return fences, fencegates and walls.
        // It just so happens that their namespace IDs contain "fence".
        if (block.namespace().asString().contains("fence")) {
            return true;
        }
        // Return all walls
        // It just so happens that their namespace IDs all end with "door".
        return block.namespace().asString().endsWith("wall");
    }

    @Override
    public boolean isClimbable() {
        // Return ladders and vines (including weeping and twisting vines)
        // Note that no other Namespace IDs contain "vine" except vines.
        return block.compare(Block.LADDER) || block.namespace().asString().contains("vine");
    }

    @Override
    public boolean isDoor() {
        // Return all normal doors and trap doors.
        // It just so happens that their namespace IDs all end with "door".
        return block.namespace().asString().endsWith("door");
    }

    @Override
    public boolean isIntractable() {
        // TODO: Interactability of blocks.
        return false;
    }

    @Override
    public boolean isImpeding() {
        return block.isSolid();
    }

    @Override
    public boolean isFullyBounded() {
        // TODO: Use Hitbox (would probably be faster as well)
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
        if (isDoor()) {
            return false;
        }
        if (block.name().startsWith("potted")) {
            return false;
        }
        // Skulls & Heads
        if (block.name().contains("skull") || block.name().contains("head")) {
            // NOTE: blocks.getName().contains("head") also matches Piston_Head
            // I could not find out by documentation if piston_head is fully bounded, I would presume it is NOT.
            return false;
        }
        // Carpets
        if (block.name().endsWith("carpet")) {
            return false;
        }
        // Slabs
        if (block.name().contains("slab")) {
            return false;
        }
        // Beds
        if (block.name().endsWith("bed")) {
            return false;
        }
        // Glass Panes
        if (block.name().endsWith("pane")) {
            return false;
        }

        return !Block.CHORUS_FLOWER.compare(block) && !Block.CHORUS_PLANT.compare(block) && !Block.BAMBOO.compare(block)
                && !Block.BAMBOO_SAPLING.compare(block) && !Block.SEA_PICKLE.compare(block)
                && !Block.TURTLE_EGG.compare(block) && !Block.SNOW.compare(block) && !Block.FLOWER_POT.compare(block)
                && !Block.LILY_PAD.compare(block) && !Block.ANVIL.compare(block) && !Block.CHIPPED_ANVIL.compare(block)
                && !Block.DAMAGED_ANVIL.compare(block) && !Block.CAKE.compare(block) && !Block.CACTUS.compare(block)
                && !Block.BREWING_STAND.compare(block) && !Block.LECTERN.compare(block)
                && !Block.DAYLIGHT_DETECTOR.compare(block) && !Block.CAMPFIRE.compare(block)
                && !Block.SOUL_CAMPFIRE.compare(block) && !Block.ENCHANTING_TABLE.compare(block)
                && !Block.CHEST.compare(block) && !Block.ENDER_CHEST.compare(block) && !Block.GRINDSTONE.compare(block)
                && !Block.TRAPPED_CHEST.compare(block) && !Block.SOUL_SAND.compare(block)
                && !Block.SOUL_SOIL.compare(block) && !Block.LANTERN.compare(block) && !Block.COCOA.compare(block)
                && !Block.CONDUIT.compare(block) && !Block.DIRT_PATH.compare(block) && !Block.FARMLAND.compare(block)
                && !Block.END_ROD.compare(block) && !Block.STONECUTTER.compare(block) && !Block.BELL.compare(block);
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
