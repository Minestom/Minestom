package net.minestom.server.event.player;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.event.trait.mutation.EventMutatorCancellable;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player tries placing a block.
 */
public record PlayerBlockPlaceEvent(@NotNull Player player, @NotNull Block block,
                                    @NotNull BlockFace blockFace, @NotNull BlockVec blockPosition,
                                    @NotNull Point cursorPosition, @NotNull PlayerHand hand, boolean consumesBlock,
                                    boolean doBlockUpdates,
                                    boolean cancelled) implements PlayerInstanceEvent, BlockEvent, CancellableEvent<PlayerBlockPlaceEvent> {

    public PlayerBlockPlaceEvent(@NotNull Player player, @NotNull Block block,
                                 @NotNull BlockFace blockFace, @NotNull BlockVec blockPosition,
                                 @NotNull Point cursorPosition, @NotNull PlayerHand hand, boolean consumesBlock) {
        this(player, block, blockFace, blockPosition, cursorPosition, hand, consumesBlock, true, false);
    }

    /**
     * Gets the block which will be placed.
     *
     * @return the block to place
     */
    @Override
    public @NotNull Block block() {
        return block;
    }

    /**
     * Gets the block position.
     *
     * @return the block position
     */
    @Override
    public @NotNull BlockVec blockPosition() {
        return blockPosition;
    }

    /**
     * Gets the hand with which the player is trying to place.
     *
     * @return the hand used
     */
    public @NotNull PlayerHand hand() {
        return hand;
    }

    /**
     * Should the block be consumed if not cancelled.
     *
     * @return true if the block will be consumed, false otherwise
     */
    public boolean consumesBlock() {
        return consumesBlock;
    }

    /**
     * Should the place trigger updates (on self and neighbors)
     * @return true if this placement should do block updates
     */
    @Override
    public boolean doBlockUpdates() {
        return doBlockUpdates;
    }

    @Override
    public @NotNull Mutator mutator() {
        return new Mutator(this);
    }

    public static class Mutator implements EventMutatorCancellable<PlayerBlockPlaceEvent> {
        private final Player player;
        private Block block;
        private final BlockFace blockFace;
        private final BlockVec blockPosition;
        private final Point cursorPosition;
        private final PlayerHand hand;

        private boolean consumesBlock;
        private boolean doBlockUpdates;

        private boolean cancelled;

        public Mutator(PlayerBlockPlaceEvent event) {
            this.player = event.player;
            this.block = event.block;
            this.blockFace = event.blockFace;
            this.blockPosition = event.blockPosition;
            this.cursorPosition = event.cursorPosition;
            this.hand = event.hand;
            this.consumesBlock = event.consumesBlock;
            this.doBlockUpdates = event.doBlockUpdates;
            this.cancelled = event.cancelled;
        }

        /**
         * Gets the block which will be placed.
         *
         * @return the block to place
         */
        public @NotNull Block getBlock() {
            return block;
        }

        /**
         * Changes the block to be placed.
         *
         * @param block the new block
         */
        public void setBlock(@NotNull Block block) {
            this.block = block;
        }

        /**
         * Should the block be consumed if not cancelled.
         *
         * @param consumeBlock true if the block should be consumer (-1 amount), false otherwise
         */
        public void setConsumesBlock(boolean consumeBlock) {
            this.consumesBlock = consumeBlock;
        }

        /**
         * Should the block be consumed if not cancelled.
         *
         * @return true if the block will be consumed, false otherwise
         */
        public boolean doesConsumeBlock() {
            return consumesBlock;
        }

        /**
         * Should the place trigger updates (on self and neighbors)
         *
         * @param doBlockUpdates true if this placement should do block updates
         */
        public void setDoBlockUpdates(boolean doBlockUpdates) {
            this.doBlockUpdates = doBlockUpdates;
        }

        /**
         * Should the place trigger updates (on self and neighbors)
         *
         * @return true if this placement should do block updates
         */
        public boolean shouldDoBlockUpdates() {
            return doBlockUpdates;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void setCancelled(boolean cancel) {
            this.cancelled = cancel;
        }

        @Override
        public @NotNull PlayerBlockPlaceEvent mutated() {
            return new PlayerBlockPlaceEvent(this.player, this.block, this.blockFace, this.blockPosition, this.cursorPosition, this.hand, this.consumesBlock, this.doBlockUpdates, this.cancelled);
        }
    }


}
