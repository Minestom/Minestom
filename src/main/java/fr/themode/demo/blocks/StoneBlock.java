package fr.themode.demo.blocks;

import net.minestom.server.data.Data;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.time.UpdateOption;

public class StoneBlock extends CustomBlock {

    public StoneBlock() {
        super((short) 1, "custom_block");
    }

    @Override
    public void onPlace(Instance instance, BlockPosition blockPosition, Data data) {

    }

    @Override
    public void onDestroy(Instance instance, BlockPosition blockPosition, Data data) {

    }

    @Override
    public void onInteract(Player player, Player.Hand hand, BlockPosition blockPosition, Data data) {

    }

    @Override
    public UpdateOption getUpdateOption() {
        return null;
    }

    @Override
    public int getBreakDelay(Player player) {
        return 750;
    }

    @Override
    public short getCustomBlockId() {
        return 2;
    }
}