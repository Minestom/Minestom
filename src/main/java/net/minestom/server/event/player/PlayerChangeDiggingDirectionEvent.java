package net.minestom.server.event.player;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

/**
 * Called when a {@link Player} keeps digging the same block but the face they are looking at changes.
 * <p>
 * The client sends this between {@link PlayerStartDiggingEvent} and either
 * {@link PlayerCancelDiggingEvent} or {@link PlayerFinishDiggingEvent}. The new face only affects
 * cosmetic effects such as the direction of the digging particles, so this event cannot be cancelled.
 */
public class PlayerChangeDiggingDirectionEvent implements PlayerInstanceEvent, BlockEvent {

    private final Player player;
    private final Instance instance;
    private final Block block;
    private final BlockVec blockPosition;
    private final BlockFace blockFace;

    @ApiStatus.Internal
    public PlayerChangeDiggingDirectionEvent(Player player, Instance instance, Block block,
                                             BlockVec blockPosition,
                                             BlockFace blockFace) {
        this.player = Objects.requireNonNull(player);
        this.instance = Objects.requireNonNull(instance);
        this.block = Objects.requireNonNull(block);
        this.blockPosition = Objects.requireNonNull(blockPosition);
        this.blockFace = Objects.requireNonNull(blockFace);
        super();
    }

    @Override
    public Instance getInstance() {
        return instance;
    }

    /**
     * Gets the block which is being dug.
     *
     * @return the block
     */
    @Override
    public Block getBlock() {
        return block;
    }

    /**
     * Gets the block position.
     *
     * @return the block position
     */
    @Override
    public BlockVec getBlockPosition() {
        return blockPosition;
    }

    /**
     * Gets the face the player is now digging.
     *
     * @return the new block face
     */
    public BlockFace getBlockFace() {
        return blockFace;
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
