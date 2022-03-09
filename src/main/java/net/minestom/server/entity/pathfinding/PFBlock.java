package net.minestom.server.entity.pathfinding;

import com.extollit.gaming.ai.path.model.IBlockDescription;
import com.extollit.gaming.ai.path.model.IBlockObject;
import com.extollit.linalg.immutable.AxisAlignedBBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
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
        final Point blockStart = this.block.registry().collisionShape().relativeStart();
        final Point blockEnd = this.block.registry().collisionShape().relativeEnd();

        return new AxisAlignedBBox(
                blockStart.x(), blockStart.y(), blockStart.z(),
                blockEnd.x(), blockEnd.y(), blockEnd.z()
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
        final Point blockStart = this.block.registry().collisionShape().relativeStart();
        final Point blockEnd = this.block.registry().collisionShape().relativeEnd();

        return blockStart.x() == 0 && blockStart.y() == 0 && blockStart.z() == 0 && blockEnd.x() == 1 && blockEnd.y() == 1 && blockEnd.z() == 1;
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
