package fr.themode.minestom.instance.demo;

import fr.themode.minestom.data.Data;
import fr.themode.minestom.entity.Player;
import fr.themode.minestom.instance.Instance;
import fr.themode.minestom.instance.block.CustomBlock;
import fr.themode.minestom.instance.block.UpdateOption;
import fr.themode.minestom.timer.TimeUnit;
import fr.themode.minestom.utils.BlockPosition;

import java.util.concurrent.atomic.AtomicInteger;

public class StoneBlock extends CustomBlock {

    private static final UpdateOption UPDATE_OPTION = new UpdateOption(1, TimeUnit.TICK);

    private final AtomicInteger counter = new AtomicInteger();

    @Override
    public UpdateOption getUpdateOption() {
        return UPDATE_OPTION;
    }

    @Override
    public void update(Instance instance, BlockPosition blockPosition, Data data) {
        if (data == null)
            return;

        data.set("value", counter.incrementAndGet(), int.class);
    }

    @Override
    public short getType() {
        return 1;
    }

    @Override
    public String getIdentifier() {
        return "custom_block";
    }

    @Override
    public int getBreakDelay(Player player) {
        return 750;
    }
}