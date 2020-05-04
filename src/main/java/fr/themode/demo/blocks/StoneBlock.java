package fr.themode.demo.blocks;

import net.minestom.server.data.Data;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.utils.BlockPosition;

public class StoneBlock extends CustomBlock {

    public StoneBlock() {
        super(Block.STONE, "custom_block");
    }

    @Override
    public void onPlace(Instance instance, BlockPosition blockPosition, Data data) {
        System.out.println("PLACED at "+blockPosition);
    }

    @Override
    public void onDestroy(Instance instance, BlockPosition blockPosition, Data data) {
        BlockPosition above = blockPosition.clone().add(0, 1, 0);
        CustomBlock blockAbove = instance.getCustomBlock(above);
        if(blockAbove == this) {
            instance.setBlock(above, Block.AIR);
            instance.setBlock(blockPosition, Block.AIR); // this should NOT create a stack overflow simply because we are trying to remove this same block
        }
    }

    @Override
    public void updateFromNeighbor(Instance instance, BlockPosition thisPosition, BlockPosition neighborPosition, boolean directNeighbor) {
        if(directNeighbor) {
            System.out.println("Block at "+thisPosition+" has been updated by neighbor at "+neighborPosition);
        }
    }

    @Override
    public boolean onInteract(Player player, Player.Hand hand, BlockPosition blockPosition, Data data) {
        return false;
    }

    @Override
    public int getBreakDelay(Player player, BlockPosition position) {
        return 750;
    }

    @Override
    public short getCustomBlockId() {
        return 2;
    }
}