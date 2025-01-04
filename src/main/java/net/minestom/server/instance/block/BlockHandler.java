package net.minestom.server.instance.block;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.NamespaceID;
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
     *
     * @param placement the placement details
     */
    default void onPlace(@NotNull Placement placement) {
    }

    /**
     * Called when a block has been destroyed or replaced.
     *
     * @param destroy the destroy details
     */
    default void onDestroy(@NotNull Destroy destroy) {
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
     * Defines custom behaviour for entities that touch this block.
     * <p>
     * This method is not continuously called when the entity is not moving.
     *
     * @param touch the contact details
     */
    default void onTouch(@NotNull Touch touch) {
    }

    default void tick(@NotNull Tick tick) {
    }

    /**
     * Use {@link #tickable()} instead as the signature is misleading.
     * <p>
     * This method is only called when the block is set.
     *
     * @return true if this block should be ticked
     */
    @Deprecated(forRemoval = true)
    default boolean isTickable() {
        return false;
    }

    /**
     * Specifies if this block should be ticked, this is immutable after the block is set.
     * <p>
     * This method is only called during the block set and later to check immutability.
     *
     * @return true if this block should be ticked
     */
    default boolean tickable() {
        return isTickable();
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
    @NotNull NamespaceID getNamespaceId();

    /**
     * Represents an object forwarded to {@link #onPlace(Placement)}.
     */
    sealed class Placement permits PlayerPlacement {
        private final Block block;
        private final Instance instance;
        private final Point blockPosition;

        @ApiStatus.Internal
        public Placement(Block block, Instance instance, Point blockPosition) {
            this.block = block;
            this.instance = instance;
            this.blockPosition = blockPosition;
        }

        /**
         * Use {@link #block()} instead.
         */
        @Deprecated(forRemoval = true)
        public @NotNull Block getBlock() {
            return block;
        }

        /**
         * Use {@link #instance()} instead.
         */
        @Deprecated(forRemoval = true)
        public @NotNull Instance getInstance() {
            return instance;
        }

        /**
         * Use {@link #blockPosition()} instead.
         */
        @Deprecated(forRemoval = true)
        public @NotNull Point getBlockPosition() {
            return blockPosition;
        }

        public @NotNull Block block() {
            return block;
        }

        public @NotNull Instance instance() {
            return instance;
        }

        public @NotNull Point blockPosition() {
            return blockPosition;
        }
    }
    /**
     * Represents an object forwarded to {@link #onPlace(Placement)} called by a player.
     */
    final class PlayerPlacement extends Placement {
        private final Player player;
        private final PlayerHand hand;
        private final BlockFace blockFace;
        private final Point cursorPosition;

        @ApiStatus.Internal
        public PlayerPlacement(Block block, Instance instance, Point blockPosition,
                               Player player, PlayerHand hand, BlockFace blockFace, Point cursorPosition) {
            super(block, instance, blockPosition);
            this.player = player;
            this.hand = hand;
            this.blockFace = blockFace;
            this.cursorPosition = cursorPosition;
        }

        /**
         * Use {@link #player()} instead.
         */
        @Deprecated(forRemoval = true)
        public @NotNull Player getPlayer() {
            return player;
        }

        /**
         * Use {@link #hand()} instead.
         */
        @Deprecated(forRemoval = true)
        public @NotNull PlayerHand getHand() {
            return hand;
        }

        /**
         * Use {@link #blockFace()} instead.
         */
        @Deprecated(forRemoval = true)
        public @NotNull BlockFace getBlockFace() {
            return blockFace;
        }

        /**
         * Use {@link #cursorPosition()} instead.
         */
        @Deprecated(forRemoval = true)
        public float getCursorX() {
            return (float) cursorPosition.x();
        }

        /**
         * Use {@link #cursorPosition()} instead.
         */
        @Deprecated(forRemoval = true)
        public float getCursorY() {
            return (float) cursorPosition.y();
        }

        /**
         * Use {@link #cursorPosition()} instead.
         */
        @Deprecated(forRemoval = true)
        public float getCursorZ() {
            return (float) cursorPosition.z();
        }

        public @NotNull Player player() {
            return player;
        }

        public @NotNull PlayerHand hand() {
            return hand;
        }

        public @NotNull BlockFace blockFace() {
            return blockFace;
        }

        public @NotNull Point cursorPosition() {
            return cursorPosition;
        }
    }

    /**
     * Represents an object forwarded to {@link #onDestroy(Destroy)}.
     */
    sealed class Destroy permits PlayerDestroy {
        private final Block block;
        private final Instance instance;
        private final Point blockPosition;

        @ApiStatus.Internal
        public Destroy(Block block, Instance instance, Point blockPosition) {
            this.block = block;
            this.instance = instance;
            this.blockPosition = blockPosition;
        }

        /**
         * Use {@link #block()} instead.
         */
        @Deprecated(forRemoval = true)
        public @NotNull Block getBlock() {
            return block;
        }

        /**
         * Use {@link #instance()} instead.
         */
        @Deprecated(forRemoval = true)
        public @NotNull Instance getInstance() {
            return instance;
        }

        /**
         * Use {@link #blockPosition()} instead.
         */
        @Deprecated(forRemoval = true)
        public @NotNull Point getBlockPosition() {
            return blockPosition;
        }

        public @NotNull Block block() {
            return block;
        }

        public @NotNull Instance instance() {
            return instance;
        }

        public @NotNull Point blockPosition() {
            return blockPosition;
        }
    }

    /**
     * Represents an object forwarded to {@link #onDestroy(Destroy)} by a player.
     */
    final class PlayerDestroy extends Destroy {
        private final Player player;

        @ApiStatus.Internal
        public PlayerDestroy(Block block, Instance instance, Point blockPosition, Player player) {
            super(block, instance, blockPosition);
            this.player = player;
        }

        public @NotNull Player player() {
            return player;
        }

        /**
         * Use {@link #player()} instead.
         */
        @Deprecated(forRemoval = true)
        public @NotNull Player getPlayer() {
            return player;
        }
    }

    /**
     * Represents an object forwarded to {@link #onInteract(Interaction)}.
     */
    record Interaction(@NotNull Block block, @NotNull Instance instance, @NotNull BlockFace blockFace,
                       @NotNull Point blockPosition, @NotNull Point cursorPosition,
                       @NotNull Player player, @NotNull PlayerHand hand) {
        @ApiStatus.Internal
        public Interaction {

        }

        /**
         * Use {@link #block()} instead.
         */
        @Deprecated(forRemoval = true)
        public @NotNull Block getBlock() {
            return block;
        }

        /**
         * Use {@link #instance()} instead.
         */
        @Deprecated(forRemoval = true)
        public @NotNull Instance getInstance() {
            return instance;
        }

        /**
         * Use {@link #blockFace()} instead.
         */
        @Deprecated(forRemoval = true)
        public @NotNull BlockFace getBlockFace() {
            return blockFace;
        }

        /**
         * Use {@link #blockPosition()} instead.
         */
        @Deprecated(forRemoval = true)
        public @NotNull Point getBlockPosition() {
            return blockPosition;
        }

        /**
         * Use {@link #cursorPosition()} instead.
         */
        @Deprecated(forRemoval = true)
        public @NotNull Point getCursorPosition() {
            return cursorPosition;
        }

        /**
         * Use {@link #player()} instead.
         */
        @Deprecated(forRemoval = true)
        public @NotNull Player getPlayer() {
            return player;
        }

        /**
         * Use {@link #hand()} instead.
         */
        @Deprecated(forRemoval = true)
        public @NotNull PlayerHand getHand() {
            return hand;
        }
    }

    /**
     * Represents an object forwarded to {@link #onTouch(Touch)}.
     */
    record Touch(@NotNull Block block, @NotNull Instance instance, @NotNull Point blockPosition,
                 @NotNull Entity touching) {

        @ApiStatus.Internal
        public Touch {

        }

        /**
         * Use {@link #block()} instead.
         */
        @Deprecated(forRemoval = true)
        public @NotNull Block getBlock() {
            return block;
        }

        /**
         * Use {@link #instance()} instead.
         */
        @Deprecated(forRemoval = true)
        public @NotNull Instance getInstance() {
            return instance;
        }

        /**
         * Use {@link #blockPosition()} instead.
         */
        @Deprecated(forRemoval = true)
        public @NotNull Point getBlockPosition() {
            return blockPosition;
        }

        /**
         * Use {@link #touching()} instead.
         */
        @Deprecated(forRemoval = true)
        public @NotNull Entity getTouching() {
            return touching;
        }
    }

    /**
     * Represents an object forwarded to {@link #tick(Tick)}.
     */
    record Tick(@NotNull Block block, @NotNull Instance instance, @NotNull Point blockPosition) {

        @ApiStatus.Internal
        public Tick {

        }

        /**
         * Use {@link #block()} instead.
         */
        @Deprecated(forRemoval = true)
        public @NotNull Block getBlock() {
            return block;
        }

        /**
         * Use {@link #instance()} instead.
         */
        @Deprecated(forRemoval = true)
        public @NotNull Instance getInstance() {
            return instance;
        }

        /**
         * Use {@link #blockPosition()} instead.
         */
        @Deprecated(forRemoval = true)
        public @NotNull Point getBlockPosition() {
            return blockPosition;
        }
    }

    /**
     * Handler used for loaded blocks with unknown namespace
     * in order to do not lose the information while saving, and for runtime debugging purpose.
     */
    @ApiStatus.Internal
    record Dummy(@NotNull NamespaceID namespace) implements BlockHandler {
        private static final Map<String, BlockHandler> DUMMY_CACHE = new ConcurrentHashMap<>();

        public static @NotNull BlockHandler get(@NotNull String namespace) {
            return DUMMY_CACHE.computeIfAbsent(namespace, Dummy::new);
        }

        private Dummy(@NotNull String name) {
            this(NamespaceID.from(name));
        }

        @Override
        public @NotNull NamespaceID getNamespaceId() {
            return namespace;
        }
    }
}
