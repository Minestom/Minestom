package fr.themode.minestom.instance;

import fr.themode.minestom.entity.Player;

public abstract class CustomBlock {

    private short type;

    public CustomBlock(short type) {
        this.type = type;
    }

    /*
      Time in ms
     */
    public abstract int getBreakDelay(Player player);

    public short getType() {
        return type;
    }
}
