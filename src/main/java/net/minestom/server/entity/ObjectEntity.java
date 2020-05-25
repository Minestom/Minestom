package net.minestom.server.entity;

import net.minestom.server.network.packet.server.play.SpawnEntityPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.Position;

public abstract class ObjectEntity extends Entity {

    public ObjectEntity(EntityType entityType, Position spawnPosition) {
        super(entityType.getId(), spawnPosition);
        setGravity(0.02f);
    }

    /**
     * Objects data can be found <a href="https://wiki.vg/Object_Data">here</a>
     *
     * @return an object data
     */
    public abstract int getObjectData();

    @Override
    public boolean addViewer(Player player) {
        PlayerConnection playerConnection = player.getPlayerConnection();

        SpawnEntityPacket spawnEntityPacket = new SpawnEntityPacket();
        spawnEntityPacket.entityId = getEntityId();
        spawnEntityPacket.uuid = getUuid();
        spawnEntityPacket.type = getEntityType();
        spawnEntityPacket.position = getPosition();
        spawnEntityPacket.data = getObjectData();
        playerConnection.sendPacket(spawnEntityPacket);
        playerConnection.sendPacket(getVelocityPacket());
        playerConnection.sendPacket(getMetadataPacket());

        if (hasPassenger()) {
            playerConnection.sendPacket(getPassengersPacket());
        }

        return super.addViewer(player); // Add player to viewers list
    }

}
