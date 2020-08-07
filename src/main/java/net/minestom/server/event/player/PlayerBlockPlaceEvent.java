package net.minestom.server.event.player;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.utils.BlockPosition;

/**
 * Called when a player tries placing a block
 */
public class PlayerBlockPlaceEvent extends CancellableEvent {

    private static final BlockManager BLOCK_MANAGER = MinecraftServer.getBlockManager();

    private final Player player;
    private short blockId;
    private short customBlockId;
    private BlockPosition blockPosition;
    private Player.Hand hand;

    private boolean consumeBlock;

    public PlayerBlockPlaceEvent(Player player, short blockId, short customBlockId, BlockPosition blockPosition, Player.Hand hand) {
        this.player = player;
        this.blockId = blockId;
        this.customBlockId = customBlockId;
        this.blockPosition = blockPosition;
        this.hand = hand;
        this.consumeBlock = true;
    }

    /**
     * Set both the blockId and customBlockId
     *
     * @param customBlock the custom block to place
     */
    public void setCustomBlock(CustomBlock customBlock) {
        setBlockId(customBlock.getBlockId());
        setCustomBlockId(customBlock.getCustomBlockId());
    }

    /**
     * Set both the blockId and customBlockId
     *
     * @param customBlockId the custom block id to place
     */
    public void setCustomBlock(short customBlockId) {
        CustomBlock customBlock = BLOCK_MANAGER.getCustomBlock(customBlockId);
        setCustomBlock(customBlock);
    }

    /**
     * Set both the blockId and customBlockId
     *
     * @param customBlockId the custom block id to place
     */
    public void setCustomBlock(String customBlockId) {
        CustomBlock customBlock = BLOCK_MANAGER.getCustomBlock(customBlockId);
        setCustomBlock(customBlock);
    }

    /**
     * Get the custom block id
     *
     * @return the custom block id
     */
    public short getCustomBlockId() {
        return customBlockId;
    }

    /**
     * Set the custom block id to place
     * <p>
     * WARNING: this does not change the visual block id, see {@link #setBlockId(short)}
     * or {@link #setCustomBlock(short)}
     *
     * @param customBlockId the custom block id
     */
    public void setCustomBlockId(short customBlockId) {
        this.customBlockId = customBlockId;
    }

    /**
     * Get the visual block id
     *
     * @return the visual block id
     */
    public short getBlockId() {
        return blockId;
    }

    /**
     * Change the visual block id
     *
     * @param blockId the new visual block id
     */
    public void setBlockId(short blockId) {
        this.blockId = blockId;
    }

    /**
     * Get the player who is placing the block
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
     * Get the hand with which the player is trying to place
     *
     * @return the hand used
     */
    public Player.Hand getHand() {
        return hand;
    }

    /**
     * Should the block be consumed if not cancelled
     *
     * @param consumeBlock true if the block should be consumer (-1 amount), false otherwise
     */
    public void consumeBlock(boolean consumeBlock) {
        this.consumeBlock = consumeBlock;
    }

    /**
     * Should the block be consumed if not cancelled
     *
     * @return true if the block will be consumed, false otherwise
     */
    public boolean doesConsumeBlock() {
        return consumeBlock;
    }
}
