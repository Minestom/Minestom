package net.minestom.server.instance.block;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
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
     * Called when a block has been placed.
     */
    default @NotNull Block onPlace(@NotNull BlockChange mutation) {
        return mutation.block();
    }

    /**
     * Called when a block has been destroyed or replaced.
     */
    default @NotNull Block onDestroy(@NotNull BlockChange mutation) {
        return Block.AIR;
    }

    /**
     * Handles interactions with this block. Can also block normal item use (containers should block when opening the
     * menu, this prevents the player from placing a block when opening it for instance).
     *
     * @param interaction the interaction details
     * @return true to let the block interaction happens, false to cancel
     */
    default boolean onInteract(@NotNull Interaction interaction) {
        return true;
    }

    /**
     * Defines custom behaviour for entities touching this block.
     *
     * @param touch the contact details
     */
    default void onTouch(@NotNull Touch touch) { }

    default void tick(@NotNull Tick tick) { }

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

    final class Interaction {
        private final Block block;
        private final Instance instance;
        private final BlockFace blockFace;
        private final Point blockPosition;
        private final Point cursorPosition;
        private final Player player;
        private final PlayerHand hand;

        @ApiStatus.Internal
        public Interaction(Block block, Instance instance, BlockFace blockFace, Point blockPosition, Point cursorPosition, Player player, PlayerHand hand) {
            this.block = block;
            this.instance = instance;
            this.blockFace = blockFace;
            this.blockPosition = blockPosition;
            this.cursorPosition = cursorPosition;
            this.player = player;
            this.hand = hand;
        }

        public @NotNull Block getBlock() {
            return block;
        }

        public @NotNull Instance getInstance() {
            return instance;
        }

        public @NotNull BlockFace getBlockFace() {
            return blockFace;
        }

        public @NotNull Point getBlockPosition() {
            return blockPosition;
        }

        public @NotNull Point getCursorPosition() {
            return cursorPosition;
        }

        public @NotNull Player getPlayer() {
            return player;
        }

        public @NotNull PlayerHand getHand() {
            return hand;
        }
    }

    final class Touch {
        private final Block block;
        private final Instance instance;
        private final Point blockPosition;
        private final Entity touching;

        @ApiStatus.Internal
        public Touch(Block block, Instance instance, Point blockPosition, Entity touching) {
            this.block = block;
            this.instance = instance;
            this.blockPosition = blockPosition;
            this.touching = touching;
        }

        public @NotNull Block getBlock() {
            return block;
        }

        public @NotNull Instance getInstance() {
            return instance;
        }

        public @NotNull Point getBlockPosition() {
            return blockPosition;
        }

        public @NotNull Entity getTouching() {
            return touching;
        }
    }

    final class Tick {
        private final Block block;
        private final Instance instance;
        private final Point blockPosition;

        @ApiStatus.Internal
        public Tick(Block block, Instance instance, Point blockPosition) {
            this.block = block;
            this.instance = instance;
            this.blockPosition = blockPosition;
        }

        public @NotNull Block getBlock() {
            return block;
        }

        public @NotNull Instance getInstance() {
            return instance;
        }

        public @NotNull Point getBlockPosition() {
            return blockPosition;
        }
    }

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
