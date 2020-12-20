package net.minestom.server.event.player;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.event.PlayerEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.utils.BlockPosition;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player tries placing a block.
 */
public class PlayerBlockPlaceEvent extends PlayerEvent implements CancellableEvent {

    private static final BlockManager BLOCK_MANAGER = MinecraftServer.getBlockManager();

    private short blockStateId;
    private short customBlockId;
    private final BlockPosition blockPosition;
    private final Player.Hand hand;

    private boolean consumeBlock;

    private boolean cancelled;

    public PlayerBlockPlaceEvent(@NotNull Player player, @NotNull Block block,
                                 @NotNull BlockPosition blockPosition, @NotNull Player.Hand hand) {
        super(player);
        this.blockStateId = block.getBlockId();
        this.blockPosition = blockPosition;
        this.hand = hand;
        this.consumeBlock = true;
    }

    /**
     * Sets both the blockId and customBlockId.
     *
     * @param customBlock the custom block to place
     */
    public void setCustomBlock(@NotNull CustomBlock customBlock) {
        setBlockStateId(customBlock.getDefaultBlockStateId());
        setCustomBlockId(customBlock.getCustomBlockId());
    }

    /**
     * Sets both the blockStateId and customBlockId.
     *
     * @param customBlockId the custom block id to place
     */
    public void setCustomBlock(short customBlockId) {
        final CustomBlock customBlock = BLOCK_MANAGER.getCustomBlock(customBlockId);
        setCustomBlock(customBlock);
    }

    /**
     * Sets both the blockId and customBlockId.
     *
     * @param customBlockId the custom block id to place
     */
    public void setCustomBlock(@NotNull String customBlockId) {
        final CustomBlock customBlock = BLOCK_MANAGER.getCustomBlock(customBlockId);
        setCustomBlock(customBlock);
    }

    /**
     * Gets the custom block id.
     *
     * @return the custom block id
     */
    public short getCustomBlockId() {
        return customBlockId;
    }

    /**
     * Sets the custom block id to place.
     * <p>
     * WARNING: this does not change the visual block id, see {@link #setBlockStateId(short)}
     * or {@link #setCustomBlock(short)}.
     *
     * @param customBlockId the custom block id
     */
    public void setCustomBlockId(short customBlockId) {
        this.customBlockId = customBlockId;
    }

    /**
     * Gets the block state id.
     *
     * @return the block state id
     */
    public short getBlockStateId() {
        return blockStateId;
    }

    /**
     * Changes the visual block id.
     *
     * @param blockStateId the new block state id
     */
    public void setBlockStateId(short blockStateId) {
        this.blockStateId = blockStateId;
    }

    /**
     * Gets the block position.
     *
     * @return the block position
     */
    @NotNull
    public BlockPosition getBlockPosition() {
        return blockPosition;
    }

    /**
     * Gets the hand with which the player is trying to place.
     *
     * @return the hand used
     */
    @NotNull
    public Player.Hand getHand() {
        return hand;
    }

    /**
     * Should the block be consumed if not cancelled.
     *
     * @param consumeBlock true if the block should be consumer (-1 amount), false otherwise
     */
    public void consumeBlock(boolean consumeBlock) {
        this.consumeBlock = consumeBlock;
    }

    /**
     * Should the block be consumed if not cancelled.
     *
     * @return true if the block will be consumed, false otherwise
     */
    public boolean doesConsumeBlock() {
        return consumeBlock;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
