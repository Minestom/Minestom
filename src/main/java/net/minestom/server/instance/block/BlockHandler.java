package net.minestom.server.instance.block;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.instance.InstanceBlockChangeEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.instance.Instance;
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
     * Called when a block in the instance changes.
     *
     * @param event The block change event.
     */
    default void onBlockChange(@NotNull InstanceBlockChangeEvent event) {}

    /**
     * Called when a player interacts with a block.
     *
     * @param event The player block interaction event.
     */
    default void onInteract(@NotNull PlayerBlockInteractEvent event) {}

    /**
     * Called when an entity touches a block.
     *
     * @param block    The block being touched.
     * @param instance The instance where the block is located.
     * @param position The position of the block.
     * @param entity   The entity touching the block.
     */
    default void onTouch(@NotNull Block block,
                         @NotNull Instance instance,
                         @NotNull Point position,
                         @NotNull Entity entity) {}

    /**
     * Called every tick if {@link BlockHandler#isTickable()} is true.
     *
     * @param block    The block being ticked.
     * @param instance The instance where the block is located.
     * @param position The position of the block.
     */
    default void tick(@NotNull Block block,
                      @NotNull Instance instance,
                      @NotNull Point position) {}

    /**
     * Determines whether the block should receive tick updates.
     *
     * @return True if the block is tickable, false otherwise.
     */
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
     * @return the key of this handler
     */
    @NotNull
    Key getKey();

    /**
     * Handler used for loaded blocks with unknown namespace
     * in order to do not lose the information while saving, and for runtime debugging purpose.
     */
    @ApiStatus.Internal
    final class Dummy implements BlockHandler {
        private static final Map<String, BlockHandler> DUMMY_CACHE = new ConcurrentHashMap<>();

        public static @NotNull BlockHandler get(@NotNull String namespace) {
            return DUMMY_CACHE.computeIfAbsent(namespace, Dummy::new);
        }

        private final Key key;

        private Dummy(String name) {
            key = Key.key(name);
        }

        @Override
        public @NotNull Key getKey() {
            return key;
        }
    }
}
