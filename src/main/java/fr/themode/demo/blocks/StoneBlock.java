package fr.themode.demo.blocks;

import fr.themode.minestom.data.Data;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.instance.Instance;
import fr.themode.minestom.instance.block.CustomBlock;
import fr.themode.minestom.utils.BlockPosition;
import fr.themode.minestom.utils.time.UpdateOption;

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
}