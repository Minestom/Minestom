package fr.themode.minestom.instance.demo;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.instance.block.CustomBlock;
import fr.themode.minestom.utils.time.UpdateOption;

public class StoneBlock extends CustomBlock {

    public StoneBlock() {
        super((short) 1, "custom_block");
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