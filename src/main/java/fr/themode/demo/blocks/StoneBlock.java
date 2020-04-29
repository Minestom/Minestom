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
        System.out.println("PLACED");
    }

    @Override
    public void onDestroy(Instance instance, BlockPosition blockPosition, Data data) {

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