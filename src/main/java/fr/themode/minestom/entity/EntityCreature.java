package fr.themode.minestom.entity;

import fr.themode.minestom.net.packet.server.play.EntityPacket;
import fr.themode.minestom.net.packet.server.play.SpawnMobPacket;
import fr.themode.minestom.net.player.PlayerConnection;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class EntityCreature extends LivingEntity {

    private Set<Player> viewers = Collections.synchronizedSet(new HashSet<>());

    private int entityType;

    public EntityCreature(int entityType) {
        super();
        this.entityType = entityType;
    }

    public void addViewer(Player player) {
        this.viewers.add(player);
        PlayerConnection playerConnection = player.getPlayerConnection();

        EntityPacket entityPacket = new EntityPacket();
        entityPacket.entityId = getEntityId();
        SpawnMobPacket spawnMobPacket = new SpawnMobPacket();
        spawnMobPacket.entityId = getEntityId();
        spawnMobPacket.entityUuid = getUuid();
        spawnMobPacket.entityType = getEntityType();
        spawnMobPacket.x = getX();
        spawnMobPacket.y = getY();
        spawnMobPacket.z = getZ();
        spawnMobPacket.yaw = getYaw();
        spawnMobPacket.pitch = getPitch();
        spawnMobPacket.headPitch = 0;
        playerConnection.sendPacket(entityPacket);
        playerConnection.sendPacket(spawnMobPacket);
    }

    public void removeViewer(Player player) {
        if (!viewers.contains(player))
            return;
        // TODO send packet to remove entity
    }

    public Set<Player> getViewers() {
        return Collections.unmodifiableSet(viewers);
    }

    public int getEntityType() {
        return entityType;
    }
}
