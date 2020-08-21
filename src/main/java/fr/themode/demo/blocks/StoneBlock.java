package fr.themode.demo.blocks;

import net.minestom.server.data.Data;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.utils.BlockPosition;

import java.util.Set;

public class StoneBlock extends CustomBlock {

    public StoneBlock() {
        super(Block.STONE, "custom_block");
    }

    @Override
    public void onPlace(Instance instance, BlockPosition blockPosition, Data data) {

    }

    @Override
    public void onDestroy(Instance instance, BlockPosition blockPosition, Data data) {
        BlockPosition above = blockPosition.clone().add(0, 1, 0);
        CustomBlock blockAbove = instance.getCustomBlock(above);
        if (blockAbove == this) {
            instance.setBlock(above, Block.AIR);
            instance.setBlock(blockPosition, Block.AIR); // this should NOT create a stack overflow simply because we are trying to remove this same block
        }
    }

    @Override
    public void updateFromNeighbor(Instance instance, BlockPosition thisPosition, BlockPosition neighborPosition, boolean directNeighbor) {

    }

    @Override
    public boolean onInteract(Player player, Player.Hand hand, BlockPosition blockPosition, Data data) {
        return false;
    }

    @Override
    public int getBreakDelay(Player player, BlockPosition position, byte stage, Set<Player> breakers) {
        return -2;
    }

    @Override
    public boolean enableCustomBreakDelay() {
        return true;
    }

    @Override
    public boolean enableMultiPlayerBreaking() {
        return true;
    }

    @Override
    public short getCustomBlockId() {
        return 2;
    }
}