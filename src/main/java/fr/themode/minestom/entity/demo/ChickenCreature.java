package fr.themode.minestom.entity.demo;

import fr.themode.minestom.entity.EntityCreature;
import fr.themode.minestom.net.packet.server.play.EntityRelativeMovePacket;

public class ChickenCreature extends EntityCreature {

    public ChickenCreature() {
        super(8);
    }

    @Override
    public void update() {
        onGround = true;

        double speed = 0.01;
        double newPos = getZ() + speed;

        EntityRelativeMovePacket entityRelativeMovePacket = new EntityRelativeMovePacket();
        entityRelativeMovePacket.entityId = getEntityId();
        entityRelativeMovePacket.deltaZ = (short) ((newPos * 32 - getZ() * 32) * 128);
        entityRelativeMovePacket.onGround = true;
        getViewers().forEach(player -> player.getPlayerConnection().sendPacket(entityRelativeMovePacket));
        setZ(newPos);
    }
}
