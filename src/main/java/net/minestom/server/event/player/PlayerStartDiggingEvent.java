package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.utils.BlockPosition;

/**
 * Called when a player start digging a {@link CustomBlock},
 * can be used to forbid the player from mining a block
 * <p>
 * WARNING: this is not called for non-custom block
 */
public class PlayerStartDiggingEvent extends CancellableEvent {

    private final Player player;
    private final BlockPosition blockPosition;
    private final CustomBlock customBlock;

    public PlayerStartDiggingEvent(Player player, BlockPosition blockPosition, CustomBlock customBlock) {
        this.player = player;
        this.blockPosition = blockPosition;
        this.customBlock = customBlock;
    }

    /**
     * Get the player who started digging the block
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the block position
     *
     * @return the block position
     */
    public BlockPosition getBlockPosition() {
        return blockPosition;
    }

    /**
     * Get the custom block object that the player is trying to dig
     *
     * @return the custom block
     */
    public CustomBlock getCustomBlock() {
        return customBlock;
    }
}
