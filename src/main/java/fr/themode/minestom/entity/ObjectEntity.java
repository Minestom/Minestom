package fr.themode.minestom.entity;

import fr.themode.minestom.Viewable;
import fr.themode.minestom.net.packet.server.play.SpawnObjectPacket;
import fr.themode.minestom.net.player.PlayerConnection;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class ObjectEntity extends Entity implements Viewable {

    private Set<Player> viewers = new CopyOnWriteArraySet<>();

    public ObjectEntity(int entityType) {
        super(entityType);
    }

    public abstract int getData();

    @Override
    public void addViewer(Player player) {
        super.addViewer(player);
        PlayerConnection playerConnection = player.getPlayerConnection();

        SpawnObjectPacket spawnObjectPacket = new SpawnObjectPacket();
        spawnObjectPacket.entityId = getEntityId();
        spawnObjectPacket.uuid = getUuid();
        spawnObjectPacket.type = getEntityType();
        spawnObjectPacket.position = getPosition();
        spawnObjectPacket.data = getData();
        playerConnection.sendPacket(spawnObjectPacket);
        playerConnection.sendPacket(getMetadataPacket());
    }

    @Override
    public void removeViewer(Player player) {
        super.removeViewer(player);
    }

    @Override
    public Set<Player> getViewers() {
        return Collections.unmodifiableSet(viewers);
    }
}
