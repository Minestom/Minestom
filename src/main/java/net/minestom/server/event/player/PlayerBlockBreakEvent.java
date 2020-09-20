package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.utils.BlockPosition;

public class PlayerBlockBreakEvent extends CancellableEvent {

    private final Player player;

    private BlockPosition blockPosition;

    private short blockStateId;
    private CustomBlock customBlock;

    private short resultBlockStateId;
    private short resultCustomBlockId;

    public PlayerBlockBreakEvent(Player player, BlockPosition blockPosition,
                                 short blockStateId, CustomBlock customBlock,
                                 short resultBlockStateId, short resultCustomBlockId) {
        this.player = player;

        this.blockPosition = blockPosition;

        this.blockStateId = blockStateId;
        this.customBlock = customBlock;

        this.resultBlockStateId = resultBlockStateId;
        this.resultCustomBlockId = resultCustomBlockId;
    }

    /**
     * Get the player who breaks the block
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
     * Get the broken block state id
     *
     * @return the block id
     */
    public short getBlockStateId() {
        return blockStateId;
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
    public short getResultBlockStateId() {
        return resultBlockStateId;
    }

    /**
     * Change the visual block id result
     *
     * @param resultBlockStateId the result block id
     */
    public void setResultBlockId(short resultBlockStateId) {
        this.resultBlockStateId = resultBlockStateId;
    }

    /**
     * Get the custom block id result, which will be placed after the event
     * <p>
     * Warning: the visual block will not be changed, be sure to call {@link #setResultBlockId(short)}
     * if you want the visual to be the same as {@link CustomBlock#getDefaultBlockStateId()} ()} ()}
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
