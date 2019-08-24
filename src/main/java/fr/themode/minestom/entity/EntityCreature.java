package fr.themode.minestom.entity;

import fr.themode.minestom.net.packet.server.play.EntityPacket;
import fr.themode.minestom.net.packet.server.play.EntityRelativeMovePacket;
import fr.themode.minestom.net.packet.server.play.SpawnMobPacket;
import fr.themode.minestom.net.player.PlayerConnection;
import fr.themode.minestom.utils.Position;

// TODO viewers synchronization each X ticks?
public abstract class EntityCreature extends LivingEntity {

    public EntityCreature(int entityType) {
        super(entityType);
    }

    @Override
    public void update() {
        super.update();
    }

    public void move(float x, float y, float z) {
        if (velocityTime != 0) {
            // Cancel movements if velocity is active
            return;
        }

        // TODO change yaw/pitch based on next position?
        Position position = getPosition();
        float newX = position.getX() + x;
        float newY = position.getY() + y;
        float newZ = position.getZ() + z;

        if (isChunkUnloaded(newX, newZ))
            return;

        EntityRelativeMovePacket entityRelativeMovePacket = new EntityRelativeMovePacket();
        entityRelativeMovePacket.entityId = getEntityId();
        entityRelativeMovePacket.deltaX = (short) ((newX * 32 - position.getX() * 32) * 128);
        entityRelativeMovePacket.deltaY = (short) ((newY * 32 - position.getY() * 32) * 128);
        entityRelativeMovePacket.deltaZ = (short) ((newZ * 32 - position.getZ() * 32) * 128);
        entityRelativeMovePacket.onGround = true;
        sendPacketToViewers(entityRelativeMovePacket);

        refreshPosition(newX, newY, newZ);
    }

    @Override
    public void kill() {
        this.isDead = true;
        triggerStatus((byte) 3);
        scheduleRemove(1000);
    }

    @Override
    public void addViewer(Player player) {
        super.addViewer(player);
        PlayerConnection playerConnection = player.getPlayerConnection();

        EntityPacket entityPacket = new EntityPacket();
        entityPacket.entityId = getEntityId();
        SpawnMobPacket spawnMobPacket = new SpawnMobPacket();
        spawnMobPacket.entityId = getEntityId();
        spawnMobPacket.entityUuid = getUuid();
        spawnMobPacket.entityType = getEntityType();
        spawnMobPacket.position = getPosition();
        spawnMobPacket.headPitch = 0;
        playerConnection.sendPacket(entityPacket);
        playerConnection.sendPacket(spawnMobPacket);
        playerConnection.sendPacket(getMetadataPacket());
    }

}
