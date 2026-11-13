package net.minestom.server.event.player;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.SignTextSlot;

import java.util.List;

public class PlayerEditSignEvent implements PlayerInstanceEvent, BlockEvent {
    private final Player player;
    private final Instance instance;
    private final Block block;
    private final BlockVec blockPosition;
    private final List<String> lines;
    private final SignTextSlot slot;

    public PlayerEditSignEvent(Player player, Instance instance, Block block, BlockVec blockPosition, List<String> lines, SignTextSlot slot) {
        this.player = player;
        this.instance = instance;
        this.block = block;
        this.blockPosition = blockPosition;
        this.lines = lines;
        this.slot = slot;
    }

    @Override
    public Instance getInstance() {
        return instance;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockVec getBlockPosition() {
        return blockPosition;
    }

    /**
     * Returns a list of strings representing the lines typed by the player onto the sign.
     * The length is always exactly 4.
     */
    public List<String> getLines() {
        return lines;
    }

    /**
     * {@return the sign text slot, the side the text is displayed on}
     */
    public SignTextSlot getTextSlot() {
        return slot;
    }

    @Deprecated
    public boolean isFrontText() {
        return slot == SignTextSlot.FRONT;
    }
}
