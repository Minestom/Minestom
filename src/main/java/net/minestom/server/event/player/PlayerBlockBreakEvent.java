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

    public BlockPosition getBlockPosition() {
        return blockPosition;
    }

    /**
     * @return the block id of the block that has been broken
     */
    public short getBlockId() {
        return blockId;
    }

    /**
     * @return the custom block of the block that has been broken,
     * null if not any
     */
    public CustomBlock getCustomBlock() {
        return customBlock;
    }

    /**
     * @return the block id that will be set at {@link #getBlockPosition()}
     * set to 0 to remove
     */
    public short getResultBlockId() {
        return resultBlockId;
    }

    /**
     * @param resultBlockId the result block id
     */
    public void setResultBlockId(short resultBlockId) {
        this.resultBlockId = resultBlockId;
    }

    /**
     * @return the custom block id that will be set at {@link #getBlockPosition()}
     * set to 0 to remove
     * <p>
     * Warning: the visual block will not be changed, be sure to call {@link #setResultBlockId(short)}
     * if you want the visual to be the same as {@link CustomBlock#getBlockId()}
     */
    public short getResultCustomBlockId() {
        return resultCustomBlockId;
    }

    /**
     * @param resultCustomBlockId the result custom block id
     */
    public void setResultCustomBlockId(short resultCustomBlockId) {
        this.resultCustomBlockId = resultCustomBlockId;
    }
}
