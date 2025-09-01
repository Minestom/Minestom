package net.minestom.server.instance.block;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.ApiStatus;

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
     * Called when a block has been placed.
     */
    default Block onPlace(BlockChange blockChange) {
        return blockChange.block();
    }

    /**
     * Called when a block has been destroyed or replaced.
     */
    default Block onDestroy(BlockChange blockChange) {
        return Block.AIR;
    }

    /**
     * Handles interactions with this block. Can also block normal item use (containers should block when opening the
     * menu, this prevents the player from placing a block when opening it for instance).
     *
     * @param blockChange the interaction details
     * @return true to let the block interaction happens, false to cancel
     */
    default boolean onInteract(BlockChange.Player blockChange) {
        return true;
    }

    /**
     * Defines custom behaviour for entities touching this block.
     *
     * @param touch the contact details
     */
    default void onTouch(Touch touch) { }

    default void tick(Tick tick) { }

    default boolean isTickable() {
        return false;
    }

    default boolean onInteract(MetadataDef.Interaction interaction) {
        return true;
    }

  /**
     * Specifies which block entity tags should be sent to the player.
     *
     * @return The list of tags from this block's block entity that should be sent to the player
     * @see <a href="https://minecraft.wiki/w/Block_entity">Block entity on the Minecraft wiki</a>
     */
    default Collection<Tag<?>> getBlockEntityTags() {
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
    Key getKey();

    record Touch(
            Block block, Instance instance,
            Point blockPosition, Entity touching
    ) {
    }

    record Tick(
            Block block, Instance instance,
            Point blockPosition
    ) {
    }

    /**
     * Handler used for loaded blocks with unknown namespace
     * in order to do not lose the information while saving, and for runtime debugging purpose.
     */
    @ApiStatus.Internal
    final class Dummy implements BlockHandler {
        private static final Map<String, BlockHandler> DUMMY_CACHE = new ConcurrentHashMap<>();
        private final Key key;

        private Dummy(String name) {
            key = Key.key(name);
        }

        public static BlockHandler get(String namespace) {
            return DUMMY_CACHE.computeIfAbsent(namespace, Dummy::new);
        }

        @Override
        public Key getKey() {
            return key;
        }
    }
}
