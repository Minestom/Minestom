package net.minestom.server.event.player;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.event.trait.mutation.EventMutatorCancellable;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a {@link Player} start digging a block,
 * can be used to forbid the {@link Player} from mining it.
 * <p>
 * Be aware that cancelling this event does not necessary prevent the player from breaking the block
 * (could be because of high latency or a modified client) so cancelling {@link PlayerBlockBreakEvent} is also necessary.
 * Could be fixed in future Minestom version.
 */
public record PlayerStartDiggingEvent(@NotNull Player player, @NotNull Block block, @NotNull BlockVec blockPosition,
                                     @NotNull BlockFace blockFace, boolean cancelled) implements PlayerInstanceEvent, BlockEvent, CancellableEvent<PlayerStartDiggingEvent> {


    public PlayerStartDiggingEvent(@NotNull Player player, @NotNull Block block, @NotNull BlockVec blockPosition,
                                   @NotNull BlockFace blockFace) {
        this(player, block, blockPosition, blockFace, false);
    }

    /**
     * Gets the block which is being dug.
     *
     * @return the block
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
     * Gets the face you are digging
     *
     * @return the block face
     */
    public @NotNull BlockFace blockFace() {
        return blockFace;
    }

    @Override
    public @NotNull Mutator mutator() {
        return new Mutator(this);
    }

    public static class Mutator extends EventMutatorCancellable.Simple<PlayerStartDiggingEvent> {
        public Mutator(@NotNull PlayerStartDiggingEvent event) {
            super(event);
        }

        @Override
        public @NotNull PlayerStartDiggingEvent mutated() {
            return new PlayerStartDiggingEvent(event.player, event.block, event.blockPosition, event.blockFace, this.isCancelled());
        }
    }

}
