package fr.themode.demo.blocks;

import fr.themode.minestom.data.Data;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.instance.Instance;
import fr.themode.minestom.instance.block.CustomBlock;
import fr.themode.minestom.utils.BlockPosition;
import fr.themode.minestom.utils.time.TimeUnit;
import fr.themode.minestom.utils.time.UpdateOption;

public class UpdatableBlockDemo extends CustomBlock {

    private static final UpdateOption UPDATE_OPTION = new UpdateOption(20, TimeUnit.TICK);

    public UpdatableBlockDemo() {
        super((short) 11, "updatable");
    }

    @Override
    public void update(Instance instance, BlockPosition blockPosition, Data data) {
        System.out.println("BLOCK UPDATE");
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
        return UPDATE_OPTION;
    }

    @Override
    public int getBreakDelay(Player player) {
        return 500;
    }
}
