package fr.themode.minestom.entity;

import fr.themode.minestom.net.packet.server.play.SpawnObjectPacket;
import fr.themode.minestom.net.player.PlayerConnection;

// TODO viewers synchronization each X ticks?
public abstract class ObjectEntity extends Entity {

    public ObjectEntity(int entityType) {
        super(entityType);
    }

    public abstract int getObjectData();

    @Override
    public void addViewer(Player player) {
        super.addViewer(player);
        PlayerConnection playerConnection = player.getPlayerConnection();

        SpawnObjectPacket spawnObjectPacket = new SpawnObjectPacket();
        spawnObjectPacket.entityId = getEntityId();
        spawnObjectPacket.uuid = getUuid();
        spawnObjectPacket.type = getEntityType();
        spawnObjectPacket.position = getPosition();
        spawnObjectPacket.data = getObjectData();
        playerConnection.sendPacket(spawnObjectPacket);
        playerConnection.sendPacket(getMetadataPacket());
    }

    @Override
    public void removeViewer(Player player) {
        super.removeViewer(player);
    }
}
