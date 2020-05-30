package net.minestom.server.event.player;

import net.minestom.server.event.CancellableEvent;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.utils.BlockPosition;

public class PlayerBlockBreakEvent extends CancellableEvent {

    private BlockPosition blockPosition;

    private short blockId;
    private CustomBlock customBlock;

    private short resultBlockId;
    private short resultCustomBlockId;

    public PlayerBlockBreakEvent(BlockPosition blockPosition,
                                 short blockId, CustomBlock customBlock,
                                 short resultBlockId, short resultCustomBlockId) {
        this.blockPosition = blockPosition;

        this.blockId = blockId;
        this.customBlock = customBlock;

        this.resultBlockId = resultBlockId;
        this.resultCustomBlockId = resultCustomBlockId;
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
     * Get the broken block visual id
     *
     * @return the block id
     */
    public short getBlockId() {
        return blockId;
    }

    /**
     * Get the broken custom block
     *
     * @return the custom block,
     * null if not any
     */
    public CustomBlock getCustomBlock() {
        return customBlock;
    }

    /**
     * Get the visual block id result, which will be placed after the event
     *
     * @return the block id that will be set at {@link #getBlockPosition()}
     * set to 0 to remove
     */
    public short getResultBlockId() {
        return resultBlockId;
    }

    /**
     * Change the visual block id result
     *
     * @param resultBlockId the result block id
     */
    public void setResultBlockId(short resultBlockId) {
        this.resultBlockId = resultBlockId;
    }

    /**
     * Get the custom block id result, which will be placed after the event
     * <p>
     * Warning: the visual block will not be changed, be sure to call {@link #setResultBlockId(short)}
     * if you want the visual to be the same as {@link CustomBlock#getBlockId()}
     *
     * @return the custom block id that will be set at {@link #getBlockPosition()}
     * set to 0 to remove
     */
    public short getResultCustomBlockId() {
        return resultCustomBlockId;
    }

    /**
     * Change the custom block id result, which will be placed after the event
     *
     * @param resultCustomBlockId the custom block id result
     */
    public void setResultCustomBlockId(short resultCustomBlockId) {
        this.resultCustomBlockId = resultCustomBlockId;
    }
}
