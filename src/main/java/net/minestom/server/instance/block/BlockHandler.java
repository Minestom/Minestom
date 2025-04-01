package net.minestom.server.instance.block;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Tag;
import org.intellij.lang.annotations.Subst;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Interface used to provide block behavior. Set with {@link Block#withHandler(BlockHandler)}.
 * These are used to be observers to block state, and are called when a block is placed, destroyed, interacted with, touched, and ticked.
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
     * @return the key of this handler
     */
    @NotNull
    Key getKey();

    @ApiStatus.Internal
    sealed interface BlockInfo {
        @SuppressWarnings("NullableProblems")
        @NotNull Block block();

        @NotNull Instance instance();

        @NotNull Point blockPosition();

        /**
         * Use {@link #block()} instead.
         */
        @Deprecated(forRemoval = true)
        default @NotNull Block getBlock() {
            return block();
        }

        /**
         * Use {@link #instance()} instead.
         */
        @Deprecated(forRemoval = true)
        default @NotNull Instance getInstance() {
            return instance();
        }

        /**
         * Use {@link #blockPosition()} instead.
         */
        @Deprecated(forRemoval = true)
        default @NotNull Point getBlockPosition() {
            return blockPosition();
        }

        sealed interface OfPlayer extends BlockInfo {
            @NotNull Player player();

            /**
             * Use {@link #player()} instead.
             */
            @Deprecated(forRemoval = true)
            default @NotNull Player getPlayer() {
                return player();
            }
        }
    }

    /**
     * Represents an object forwarded to {@link #onPlace(Placement)}.
     */
    sealed interface Placement extends BlockInfo {
        /**
         * Represents an object forwarded to {@link #onPlace(Placement)} by a player.
         */
        record OfPlayer(@NotNull Block block, @NotNull Instance instance, @NotNull Point blockPosition,
                      @NotNull Player player, @NotNull PlayerHand hand, @NotNull BlockFace blockFace,
                      @NotNull Point cursorPosition) implements Placement, BlockInfo.OfPlayer, PlayerPlacement {

            @ApiStatus.Internal
            public OfPlayer {
                Check.notNull(block, "Block cannot be null");
                Check.notNull(instance, "Instance cannot be null");
                Check.notNull(blockPosition, "Block position cannot be null");
                Check.notNull(player, "Player cannot be null");
                Check.notNull(hand, "Hand cannot be null");
                Check.notNull(blockFace, "Block face cannot be null");
                Check.notNull(cursorPosition, "Cursor position cannot be null");
            }
        }
        /**
         * Represents an object forwarded to {@link #onPlace(Placement)} by an unknown source.
         * <p>
         * Used as a fallback when the source of the placement is unknown.
         */
        record OfUnknown(@NotNull Block block, @NotNull Instance instance,
                         @NotNull Point blockPosition) implements Placement {
        }

        /**
         * Represents your own implementation of {@link Placement}.
         * <p>
         * You can use these events by calling {@link net.minestom.server.instance.InstanceContainer#placeBlock(Placement)}
         */
        @ApiStatus.Experimental
        non-sealed interface OfCustom extends Placement {}
    }

    /**
     * Represents an object forwarded to {@link #onDestroy(Destroy)}.
     */
    sealed interface Destroy extends BlockInfo {
        /**
         * Represents an object forwarded to {@link #onDestroy(Destroy)} by a player.
         */
        record OfPlayer(@NotNull Block block, @NotNull Instance instance, @NotNull Point blockPosition,
                      @NotNull Player player, @NotNull BlockFace blockFace) implements Destroy, BlockInfo.OfPlayer, PlayerDestroy {

            @ApiStatus.Internal
            public OfPlayer {
                Check.notNull(block, "Block cannot be null");
                Check.notNull(instance, "Instance cannot be null");
                Check.notNull(blockPosition, "Block position cannot be null");
                Check.notNull(player, "Player cannot be null");
                Check.notNull(blockFace, "Block face cannot be null");
            }
        }

        /**
         * Represents an object forwarded to {@link #onDestroy(Destroy)} by an unknown source.
         * <p>
         * Used as a fallback when the source of the destruction is unknown.
         */
        record OfUnknown(@NotNull Block block, @NotNull Instance instance,
                         @NotNull Point blockPosition) implements Destroy {

            @ApiStatus.Internal
            public OfUnknown {
                Check.notNull(block, "Block cannot be null");
                Check.notNull(instance, "Instance cannot be null");
                Check.notNull(blockPosition, "Block position cannot be null");
            }
        }
        /**
         * Represents your own implementation of {@link Destroy}.
         * <p>
         * You can use these events by calling {@link net.minestom.server.instance.InstanceContainer#breakBlock(Player, Point, BlockFace, boolean, PlayerSupplier)}
         * or {@link net.minestom.server.instance.InstanceContainer#breakBlock(Destroy, boolean)}
         */
        @ApiStatus.Experimental
        non-sealed interface OfCustom extends Destroy {
            @Override
            @UnknownNullability Block block();
        }


        @FunctionalInterface
        interface PlayerSupplier {
            Destroy create(@NotNull Block block, @NotNull Instance instance, @NotNull Point blockPosition, @NotNull Player player, @NotNull BlockFace blockFace);
        }
    }

    /**
     * Represents an object forwarded to {@link #onInteract(Interaction)}.
     */
    record Interaction(@NotNull Block block, @NotNull Instance instance, @NotNull BlockFace blockFace,
                       @NotNull Point blockPosition, @NotNull Point cursorPosition,
                       @NotNull Player player, @NotNull PlayerHand hand) implements BlockInfo, BlockInfo.OfPlayer {
        @ApiStatus.Internal
        public Interaction {
            Check.notNull(block, "Block cannot be null");
            Check.notNull(instance, "Instance cannot be null");
            Check.notNull(blockFace, "Block face cannot be null");
            Check.notNull(blockPosition, "Block position cannot be null");
            Check.notNull(cursorPosition, "Cursor position cannot be null");
            Check.notNull(player, "Player cannot be null");
            Check.notNull(hand, "Player's hand cannot be null");
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
        public @NotNull Point getCursorPosition() {
            return cursorPosition;
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
                 @NotNull Entity touching) implements BlockInfo {

        @ApiStatus.Internal
        public Touch {
            Check.notNull(block, "Block cannot be null");
            Check.notNull(instance, "Instance cannot be null");
            Check.notNull(blockPosition, "Block position cannot be null");
            Check.notNull(touching, "Touching entity cannot be null");
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
    record Tick(@NotNull Block block, @NotNull Instance instance, @NotNull Point blockPosition) implements BlockInfo {

        @ApiStatus.Internal
        public Tick {
            Check.notNull(block, "Block cannot be null");
            Check.notNull(instance, "Instance cannot be null");
            Check.notNull(blockPosition, "Block position cannot be null");
        }
    }

    /**
     * Handler used for loaded blocks with unknown namespace/key
     * in order to do not lose the information while saving, and for runtime debugging purpose.
     */
    @ApiStatus.Internal
    record Dummy(@NotNull Key key) implements BlockHandler {
        private static final Map<String, BlockHandler> DUMMY_CACHE = new ConcurrentHashMap<>();

        public Dummy {
            Check.notNull(key, "Key cannot be null");
        }

        private Dummy(@Subst("dummy") @NotNull String name) {
            this(Key.key(name));
        }

        @Override
        public @NotNull Key getKey() {
            return key;
        }

        public static @NotNull BlockHandler get(@NotNull String key) {
            return DUMMY_CACHE.computeIfAbsent(key, Dummy::new);
        }
    }

    // TODO delete these.

    /**
     * @deprecated Use {@link Placement.OfPlayer} instead.
     * <p>
     * Represents an object forwarded to {@link #onPlace(Placement)} called by a player.
     */
    @Deprecated(forRemoval = true)
    @SuppressWarnings("DeprecatedIsStillUsed") // Used in Placement.OfPlayer
    sealed interface PlayerPlacement extends Placement, BlockInfo.OfPlayer {

        /**
         * Use {@link #hand()} instead.
         */
        @Deprecated(forRemoval = true)
        default @NotNull PlayerHand getHand() {
            return hand();
        }

        /**
         * Use {@link #blockFace()} instead.
         */
        @Deprecated(forRemoval = true)
        default @NotNull BlockFace getBlockFace() {
            return blockFace();
        }

        /**
         * Use {@link #cursorPosition()} instead.
         */
        @Deprecated(forRemoval = true)
        default float getCursorX() {
            return (float) cursorPosition().x();
        }

        /**
         * Use {@link #cursorPosition()} instead.
         */
        @Deprecated(forRemoval = true)
        default float getCursorY() {
            return (float) cursorPosition().y();
        }

        /**
         * Use {@link #cursorPosition()} instead.
         */
        @Deprecated(forRemoval = true)
        default float getCursorZ() {
            return (float) cursorPosition().z();
        }

        @NotNull PlayerHand hand();

        @NotNull BlockFace blockFace();

        @NotNull Point cursorPosition();
    }

    /**
     * @deprecated Use {@link Destroy.OfPlayer} instead.
     *
     * Represents an object forwarded to {@link #onDestroy(Destroy)} by a player.
     */
    @Deprecated(forRemoval = true)
    @SuppressWarnings("DeprecatedIsStillUsed")  // Used in Destroy.OfPlayer
    sealed interface PlayerDestroy extends Destroy, BlockInfo.OfPlayer {

    }
}
