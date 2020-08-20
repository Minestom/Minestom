package fr.themode.demo.blocks;

import net.minestom.server.data.Data;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.CustomBlock;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.time.UpdateOption;

import java.util.Set;

public class UpdatableBlockDemo extends CustomBlock {

    private static final UpdateOption UPDATE_OPTION = new UpdateOption(20, TimeUnit.TICK);

    public UpdatableBlockDemo() {
        super(Block.DIRT, "updatable");
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
    public boolean onInteract(Player player, Player.Hand hand, BlockPosition blockPosition, Data data) {
        return false;
    }

    @Override
    public UpdateOption getUpdateOption() {
        return UPDATE_OPTION;
    }

    @Override
    public int getBreakDelay(Player player, BlockPosition position, byte stage, Set<Player> breakers) {
        return 1;
    }

    @Override
    public boolean enableCustomBreakDelay() {
        return true;
    }

    @Override
    public short getCustomBlockId() {
        return 1;
    }
}
