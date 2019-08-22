package fr.themode.minestom.instance.demo;

import fr.themode.minestom.entity.Player;
import fr.themode.minestom.instance.CustomBlock;

public class StoneBlock extends CustomBlock {

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