package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.NoSuchElementException;

/**
 *  The result of a {@link Ray#cast(Block.Getter)}
 */
public interface BlockCastResult extends CastResult {
    /**
     * Whether this {@link BlockCastResult} has a block collision or not
     *
     * @return true if there is a block collision, otherwise false
     */
    boolean hasBlockCollision();

    /**
     * Returns the first block collision from the proceeding {@link Ray#cast}
     *
     * @return the first block collision
     * @throws NoSuchElementException when the block cast result has no {@link BlockRayCollision}
     */
    @NotNull BlockRayCollision firstBlockCollision();

    /**
     * Returns the last block collision from the proceeding {@link Ray#cast}
     *
     * @return the last block collision
     * @throws NoSuchElementException when the block cast result has no {@link BlockRayCollision}
     */
    @NotNull BlockRayCollision lastBlockCollision();

    /**
     * Returns an ordered immutable {@link BlockRayCollision} list from the proceeding {@link Ray#cast}.
     *
     * @return the list of block collisions
     */
    @NotNull List<BlockRayCollision> blockCollisions();

    record BlockRayCollision(@NotNull Point entry, @NotNull Point exit, @Nullable Vec entrySurfaceNormal,
                             @Nullable Vec exitSurfaceNormal, @NotNull Block block) implements CastResult.RayCollision {
        @Override
        public @NotNull Vec entrySurfaceNormal() {
            if (entrySurfaceNormal == null) {
                throw new NoSuchElementException("Cast ray with Ray.Configuration.computeSurfaceNormals = true");
            }
            return entrySurfaceNormal;
        }

        @Override
        public @NotNull Vec exitSurfaceNormal() {
            if (exitSurfaceNormal == null) {
                throw new NoSuchElementException("Cast ray with Ray.Configuration.computeSurfaceNormals = true");
            }
            return exitSurfaceNormal;
        }
    }
}
