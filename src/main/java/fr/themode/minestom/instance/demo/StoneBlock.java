package fr.themode.minestom.instance.demo;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.instance.CustomBlock;

public class StoneBlock extends CustomBlock {

    public StoneBlock() {
        super((short) 1);
    }

    @Override
    public int getBreakDelay(Player player) {
        return 750;
    }
}
