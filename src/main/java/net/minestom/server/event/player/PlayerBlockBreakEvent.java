package net.minestom.server.event.player;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.event.trait.mutation.EventMutatorCancellable;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record PlayerBlockBreakEvent(@NotNull Player player,
                                    @NotNull Block block, @NotNull Block resultBlock, @NotNull BlockVec blockPosition,
                                    @NotNull BlockFace blockFace, boolean cancelled) implements PlayerInstanceEvent, BlockEvent, CancellableEvent<PlayerBlockBreakEvent> {

    public PlayerBlockBreakEvent(@NotNull Player player,
                                 @NotNull Block block, @NotNull Block resultBlock, @NotNull BlockVec blockPosition,
                                 @NotNull BlockFace blockFace) {
        this(player, block, resultBlock, blockPosition, blockFace, false);
    }

    /**
     * Gets the block to break
     *
     * @return the block
     */
    @Override
    public @NotNull Block block() {
        return block;
    }

    /**
     * Gets the block which will replace {@link #block()}.
     *
     * @return the result block
     */
    @Override
    public @NotNull Block resultBlock() {
        return resultBlock;
    }

    /**
     * Gets the face at which the block was broken
     *
     * @return the block face
     */
    @Override
    public @NotNull BlockFace blockFace() {
        return blockFace;
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

    @Override
    public @NotNull Mutator mutator() {
        return new Mutator(this);
    }

    public static class Mutator implements EventMutatorCancellable<PlayerBlockBreakEvent> {
        private final Player player;
        private final Block block;
        private Block resultBlock;
        private final BlockVec blockPosition;
        private final BlockFace blockFace;


        private boolean cancelled;

        public Mutator(PlayerBlockBreakEvent event) {
            this.player = event.player;
            this.block = event.block;
            this.resultBlock = event.resultBlock;
            this.blockPosition = event.blockPosition;
            this.blockFace = event.blockFace;
            this.cancelled = event.cancelled;
        }

        /**
         * Changes the result of the event.
         *
         * @param resultBlock the new block
         */
        public void setResultBlock(@NotNull Block resultBlock) {
            this.resultBlock = resultBlock;
        }

        /**
         * Gets the block which will replace {@link #block()}.
         *
         * @return the result block
         */
        public @NotNull Block getResultBlock() {
            return resultBlock;
        }

        @Override
        public boolean isCancelled() {
            return this.cancelled;
        }

        @Override
        public void setCancelled(boolean cancel) {
            this.cancelled = cancel;
        }

        @Contract(pure = true)
        @Override
        public @NotNull PlayerBlockBreakEvent mutated() {
            return new PlayerBlockBreakEvent(this.player, this.block, this.resultBlock, this.blockPosition, this.blockFace, this.cancelled);
        }
    }
}
