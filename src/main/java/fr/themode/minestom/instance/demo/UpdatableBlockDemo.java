package fr.themode.minestom.instance.demo;

import fr.themode.minestom.data.Data;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.instance.Instance;
import fr.themode.minestom.instance.block.CustomBlock;
import fr.themode.minestom.instance.block.UpdateOption;
import fr.themode.minestom.utils.BlockPosition;
import fr.themode.minestom.utils.time.TimeUnit;

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
    public UpdateOption getUpdateOption() {
        return UPDATE_OPTION;
    }

    @Override
    public int getBreakDelay(Player player) {
        return 500;
    }
}
