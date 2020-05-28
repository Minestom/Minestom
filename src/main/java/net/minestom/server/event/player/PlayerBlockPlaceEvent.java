package net.minestom.server.event.player;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.utils.BlockPosition;

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
     * @return the custom block id to place
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
     * @param customBlockId
     */
    public void setCustomBlockId(short customBlockId) {
        this.customBlockId = customBlockId;
    }

    /**
     * @return the visual block id to place
     */
    public short getBlockId() {
        return blockId;
    }

    /**
     * @param blockId the visual block id to place
     */
    public void setBlockId(short blockId) {
        this.blockId = blockId;
    }

    /**
     * @return the player who is placing the block
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * @return the position of the block to place
     */
    public BlockPosition getBlockPosition() {
        return blockPosition;
    }

    /**
     * @return the hand with which the player is trying to place
     */
    public Player.Hand getHand() {
        return hand;
    }

    /**
     * @param consumeBlock true if the block should be consumer (-1 amount), false otherwise
     */
    public void consumeBlock(boolean consumeBlock) {
        this.consumeBlock = consumeBlock;
    }

    /**
     * @return true if the block will be consumed, false otherwise
     */
    public boolean doesConsumeBlock() {
        return consumeBlock;
    }
}
