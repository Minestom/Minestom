package net.minestom.server.entity.pathfinding;

import com.extollit.gaming.ai.path.model.IBlockDescription;
import com.extollit.gaming.ai.path.model.IBlockObject;
import com.extollit.linalg.immutable.AxisAlignedBBox;
import net.minestom.server.collision.Shape;
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
        Shape shape = this.block.registry().collisionShape();
        return new AxisAlignedBBox(
                shape.relativeStart().x(), shape.relativeStart().y(), shape.relativeStart().z(),
                shape.relativeEnd().x(), shape.relativeEnd().y(), shape.relativeEnd().z()
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
        // It just so happens that their namespace IDs all end with "wall".
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
        Shape shape = block.registry().collisionShape();
        return shape.relativeStart().isZero()
                && shape.relativeEnd().x() == 1.0d
                && shape.relativeEnd().y() == 1.0d
                && shape.relativeEnd().z() == 1.0d;
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
