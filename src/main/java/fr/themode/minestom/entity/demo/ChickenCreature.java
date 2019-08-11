package fr.themode.minestom.entity.demo;

import fr.themode.minestom.entity.EntityCreature;

public class ChickenCreature extends EntityCreature {

    public ChickenCreature() {
        super(8);
    }

    @Override
    public void update() {
        double speed = 0.075;

        /*Player player = Main.getConnectionManager().getPlayer("TheMode911");
        if (player != null) {
            teleport(player.getX(), 5, player.getZ());
        }*/

        move(0, 0, speed);
    }
}
