package net.minestom.server.event.player;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.event.PlayerEvent;
import net.minestom.server.instance.block.BlockManager;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerBlockBreakEvent extends PlayerEvent implements CancellableEvent {

    private static final BlockManager BLOCK_MANAGER = MinecraftServer.getBlockManager();

    private final BlockPosition blockPosition;

    private final short blockStateId;
    private final CustomBlock customBlock;

    private short resultBlockStateId;
    private short resultCustomBlockId;

    private boolean cancelled;

    public PlayerBlockBreakEvent(@NotNull Player player, @NotNull BlockPosition blockPosition,
                                 short blockStateId, @Nullable CustomBlock customBlock,
                                 short resultBlockStateId, short resultCustomBlockId) {
        super(player);

        this.blockPosition = blockPosition;

        this.blockStateId = blockStateId;
        this.customBlock = customBlock;

        this.resultBlockStateId = resultBlockStateId;
        this.resultCustomBlockId = resultCustomBlockId;
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
     * Gets the broken block state id.
     *
     * @return the block id
     */
    public short getBlockStateId() {
        return blockStateId;
    }

    /**
     * Gets the broken custom block.
     *
     * @return the custom block,
     * null if not any
     */
    @Nullable
    public CustomBlock getCustomBlock() {
        return customBlock;
    }

    /**
     * Gets the visual block id result, which will be placed after the event.
     *
     * @return the block id that will be set at {@link #getBlockPosition()}
     * set to 0 to remove
     */
    public short getResultBlockStateId() {
        return resultBlockStateId;
    }

    /**
     * Changes the visual block id result.
     *
     * @param resultBlockStateId the result block id
     */
    public void setResultBlockId(short resultBlockStateId) {
        this.resultBlockStateId = resultBlockStateId;
    }

    /**
     * Gets the custom block id result, which will be placed after the event.
     * <p>
     * Warning: the visual block will not be changed, be sure to call {@link #setResultBlockId(short)}
     * if you want the visual to be the same as {@link CustomBlock#getDefaultBlockStateId()}.
     *
     * @return the custom block id that will be set at {@link #getBlockPosition()}
     * set to 0 to remove
     */
    public short getResultCustomBlockId() {
        return resultCustomBlockId;
    }

    /**
     * Changes the custom block id result, which will be placed after the event.
     *
     * @param resultCustomBlockId the custom block id result
     */
    public void setResultCustomBlockId(short resultCustomBlockId) {
        this.resultCustomBlockId = resultCustomBlockId;
    }

    /**
     * Sets both the blockId and customBlockId.
     *
     * @param customBlock the result custom block
     */
    public void setResultCustomBlock(@NotNull CustomBlock customBlock) {
        setResultBlockId(customBlock.getDefaultBlockStateId());
        setResultCustomBlockId(customBlock.getCustomBlockId());
    }

    /**
     * Sets both the blockStateId and customBlockId.
     *
     * @param customBlockId the result custom block
     */
    public void setResultCustomBlock(short customBlockId) {
        final CustomBlock customBlock = BLOCK_MANAGER.getCustomBlock(customBlockId);
        Check.notNull(customBlock, "The custom block with the id '" + customBlockId + "' does not exist");
        setResultCustomBlock(customBlock);
    }

    /**
     * Sets both the blockId and customBlockId.
     *
     * @param customBlockId the result custom block id
     */
    public void setResultCustomBlock(@NotNull String customBlockId) {
        final CustomBlock customBlock = BLOCK_MANAGER.getCustomBlock(customBlockId);
        Check.notNull(customBlock, "The custom block with the identifier '" + customBlockId + "' does not exist");
        setResultCustomBlock(customBlock);
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
