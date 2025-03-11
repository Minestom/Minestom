package net.minestom.server.instance.block;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.block.BlockChangeEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Interface used to provide block behavior. Set with {@link Block#withHandler(BlockHandler)}.
 * <p>
 * Implementations are expected to be thread safe.
 */
public interface BlockHandler {

    /**
     * Called when a block has been modified.
     */
    default void onBlockChange(@NotNull BlockChangeEvent event) { }

    /**
     * Handles interactions with this block. Can also block normal item use (containers should block when opening the
     * menu, this prevents the player from placing a block when opening it for instance).
     */
    default boolean onInteract(@NotNull PlayerBlockInteractEvent event) {
        return true;
    }

    /**
     * Defines custom behaviour for entities touching this block.
     */
    default void onTouch(@NotNull Block block,
                         @NotNull Instance instance,
                         @NotNull Vec position,
                         @NotNull Entity entity) { }

    default Block onNeighborUpdate(@NotNull Block neighbor,
                                  @NotNull Instance instance,
                                  @NotNull Point point,
                                  @NotNull BlockFace fromFace) {
        return neighbor;
    }

    default boolean isSelfReplaceable(@NotNull Block block,
                                      @NotNull BlockFace blockFace,
                                      @NotNull Point cursorPosition,
                                      @NotNull Material material) {
        return false;
    }

    default void tick(@NotNull Block block,
                      @NotNull Instance instance,
                      @NotNull Point position) { }

    default boolean isTickable() {
        return false;
    }

    /**
     * Specifies which block entity tags should be sent to the player.
     *
     * @return The list of tags from this block's block entity that should be sent to the player
     * @see <a href="https://minecraft.wiki/w/Block_entity">Block entity on the Minecraft wiki</a>
     */
    default @NotNull Collection<Tag<?>> getBlockEntityTags() {
        return List.of();
    }

    default byte getBlockEntityAction() {
        return -1;
    }

    /**
     * Gets the id of this handler.
     * <p>
     * Used to write the block entity in the anvil world format.
     *
     * @return the namespace id of this handler
     */
    @NotNull Block getBlock();

    /**
     * Handler used for loaded blocks with unknown key
     * in order to do not lose the information while saving, and for runtime debugging purpose.
     */
    @ApiStatus.Internal
    final class Dummy implements BlockHandler {
        private static final Map<Block, BlockHandler> DUMMY_CACHE = new ConcurrentHashMap<>();

        public static @NotNull BlockHandler get(Block block) {
            return DUMMY_CACHE.computeIfAbsent(block, Dummy::new);
        }

        private final Block block;

        private Dummy(Block block) {
            this.block = block;
        }

        @Override
        public @NotNull Block getBlock() {
            return block;
        }
    }
}
