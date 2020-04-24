package net.minestom.server.entity;

import net.minestom.server.network.packet.server.play.SpawnEntityPacket;
import net.minestom.server.network.player.PlayerConnection;

public abstract class ObjectEntity extends Entity {

    public ObjectEntity(int entityType) {
        super(entityType);
    }

    public abstract int getObjectData();

    @Override
    public void addViewer(Player player) {
        PlayerConnection playerConnection = player.getPlayerConnection();

        SpawnEntityPacket spawnEntityPacket = new SpawnEntityPacket();
        spawnEntityPacket.entityId = getEntityId();
        spawnEntityPacket.uuid = getUuid();
        spawnEntityPacket.type = getEntityType();
        spawnEntityPacket.position = getPosition();
        spawnEntityPacket.data = getObjectData();
        playerConnection.sendPacket(spawnEntityPacket);
        playerConnection.sendPacket(getMetadataPacket());
        super.addViewer(player); // Add player to viewers list and send velocity packet
    }

    @Override
    public void removeViewer(Player player) {
        super.removeViewer(player);
    }

}
