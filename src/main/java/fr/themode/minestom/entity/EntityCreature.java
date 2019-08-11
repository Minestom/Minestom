package fr.themode.minestom.entity;

import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.play.EntityPacket;
import fr.themode.minestom.net.packet.server.play.EntityRelativeMovePacket;
import fr.themode.minestom.net.packet.server.play.EntityTeleportPacket;
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

    public void move(double x, double y, double z) {
        double newX = getX() + x;
        double newY = getY() + y;
        double newZ = getZ() + z;

        if (chunkTest(newX, newZ))
            return;

        EntityRelativeMovePacket entityRelativeMovePacket = new EntityRelativeMovePacket();
        entityRelativeMovePacket.entityId = getEntityId();
        entityRelativeMovePacket.deltaX = (short) ((newX * 32 - getX() * 32) * 128);
        entityRelativeMovePacket.deltaY = (short) ((newY * 32 - getY() * 32) * 128);
        entityRelativeMovePacket.deltaZ = (short) ((newZ * 32 - getZ() * 32) * 128);
        entityRelativeMovePacket.onGround = true;
        sendPacketToViewers(entityRelativeMovePacket);

        refreshPosition(newX, newY, newZ);
    }

    public void teleport(double x, double y, double z) {
        if (chunkTest(x, z))
            return;

        EntityTeleportPacket entityTeleportPacket = new EntityTeleportPacket();
        entityTeleportPacket.entityId = getEntityId();
        entityTeleportPacket.x = x;
        entityTeleportPacket.y = y;
        entityTeleportPacket.z = z;
        entityTeleportPacket.yaw = getYaw();
        entityTeleportPacket.pitch = getPitch();
        entityTeleportPacket.onGround = true;
        sendPacketToViewers(entityTeleportPacket);
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
        synchronized (viewers) {
            if (!viewers.contains(player))
                return;
            this.viewers.remove(player);
            // TODO send packet to remove entity
        }
    }

    protected void sendPacketToViewers(ServerPacket packet) {
        getViewers().forEach(player -> player.getPlayerConnection().sendPacket(packet));
    }

    public Set<Player> getViewers() {
        return Collections.unmodifiableSet(viewers);
    }

    public int getEntityType() {
        return entityType;
    }
}
