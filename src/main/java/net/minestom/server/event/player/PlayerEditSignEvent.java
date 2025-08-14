package net.minestom.server.event.player;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.instance.block.Block;

import java.util.List;

public class PlayerEditSignEvent implements PlayerInstanceEvent, BlockEvent {
    private final Player player;
    private final Block block;
    private final BlockVec blockPosition;
    private final List<String> lines;
    private final boolean isFrontText;

    public PlayerEditSignEvent(Player player, Block block, BlockVec blockPosition, List<String> lines, boolean isFrontText) {
        this.player = player;
        this.block = block;
        this.blockPosition = blockPosition;
        this.lines = lines;
        this.isFrontText = isFrontText;
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

    public boolean isFrontText() {
        return isFrontText;
    }
}
