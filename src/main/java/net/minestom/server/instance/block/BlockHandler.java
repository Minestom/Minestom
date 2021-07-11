package net.minestom.server.instance.block;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
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
     * @return true if this block blocks normal item use, false otherwise
     */
    default boolean onInteract(@NotNull Interaction interaction) {
        return false;
    }

    /**
     * Defines custom behaviour for entities touching this block.
     *
     * @param touch the contact details
     */
    default void onTouch(@NotNull Touch touch) {
    }

    default void tick(@NotNull Tick tick) {
    }

    default boolean isTickable() {
        return false;
    }

    default @NotNull Collection<Tag<?>> getBlockEntityTags() {
        return Collections.emptyList();
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
     * <p>
     * Will in the future rely on sealed classes (https://openjdk.java.net/jeps/409)
     * and record pattern for the implementations (https://openjdk.java.net/jeps/405).
     */
    @ApiStatus.NonExtendable
    class Placement {
        private final Block block;
        private final Instance instance;
        private final Point blockPosition;

        @ApiStatus.Internal
        public Placement(Block block, Instance instance, Point blockPosition) {
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

    final class PlayerPlacement extends Placement {
        private final Player player;
        private final BlockFace blockFace;
        private final float cursorX, cursorY, cursorZ;

        @ApiStatus.Internal
        public PlayerPlacement(Block block, Instance instance, Point blockPosition,
                               Player player, BlockFace blockFace, float cursorX, float cursorY, float cursorZ) {
            super(block, instance, blockPosition);
            this.player = player;
            this.blockFace = blockFace;
            this.cursorX = cursorX;
            this.cursorY = cursorY;
            this.cursorZ = cursorZ;
        }

        public @NotNull Player getPlayer() {
            return player;
        }

        public @NotNull BlockFace getBlockFace() {
            return blockFace;
        }

        public float getCursorX() {
            return cursorX;
        }

        public float getCursorY() {
            return cursorY;
        }

        public float getCursorZ() {
            return cursorZ;
        }
    }

    @ApiStatus.NonExtendable
    class Destroy {
        private final Block block;
        private final Instance instance;
        private final Point blockPosition;

        @ApiStatus.Internal
        public Destroy(Block block, Instance instance, Point blockPosition) {
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

    final class PlayerDestroy extends Destroy {
        private final Player player;

        @ApiStatus.Internal
        public PlayerDestroy(Block block, Instance instance, Point blockPosition, Player player) {
            super(block, instance, blockPosition);
            this.player = player;
        }

        public @NotNull Player getPlayer() {
            return player;
        }
    }

    @ApiStatus.NonExtendable
    class Interaction {
        private final Block block;
        private final Instance instance;
        private final Point blockPosition;
        private final Player player;
        private final Player.Hand hand;

        @ApiStatus.Internal
        public Interaction(Block block, Instance instance, Point blockPosition, Player player, Player.Hand hand) {
            this.block = block;
            this.instance = instance;
            this.blockPosition = blockPosition;
            this.player = player;
            this.hand = hand;
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

        public @NotNull Player getPlayer() {
            return player;
        }

        public @NotNull Player.Hand getHand() {
            return hand;
        }
    }

    @ApiStatus.NonExtendable
    class Touch {
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

    @ApiStatus.NonExtendable
    class Tick {
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
    class Dummy implements BlockHandler {
        private static final Map<String, BlockHandler> DUMMY_CACHE = new ConcurrentHashMap<>();

        @ApiStatus.Internal
        public static @NotNull BlockHandler get(@NotNull String namespace) {
            return DUMMY_CACHE.computeIfAbsent(namespace, s -> new Dummy(NamespaceID.from(namespace)));
        }

        private final NamespaceID namespaceID;

        private Dummy(NamespaceID namespaceID) {
            this.namespaceID = namespaceID;
        }

        @Override
        public @NotNull NamespaceID getNamespaceId() {
            return namespaceID;
        }
    }
}
