package fr.themode.minestom.entity;

import fr.themode.minestom.event.DeathEvent;
import fr.themode.minestom.net.packet.server.play.EntityHeadLookPacket;
import fr.themode.minestom.net.packet.server.play.EntityLookAndRelativeMovePacket;
import fr.themode.minestom.net.packet.server.play.EntityPacket;
import fr.themode.minestom.net.packet.server.play.SpawnMobPacket;
import fr.themode.minestom.net.player.PlayerConnection;
import fr.themode.minestom.utils.ChunkUtils;
import fr.themode.minestom.utils.EntityUtils;
import fr.themode.minestom.utils.Position;

// TODO pathfinding
public abstract class EntityCreature extends LivingEntity {

    public EntityCreature(EntityType entityType) {
        super(entityType.getId());
    }

    @Override
    public void update() {
        super.update();
    }

    public void move(float x, float y, float z) {
        // TODO change yaw/pitch based on next position?
        Position position = getPosition();
        float newX = position.getX() + x;
        float newY = position.getY() + y;
        float newZ = position.getZ() + z;

        if (ChunkUtils.isChunkUnloaded(getInstance(), newX, newZ))
            return;

        float lastYaw = position.getYaw();
        float radians = (float) Math.atan2(newZ - position.getZ(), newX - position.getX());

        float yaw = (float) (radians * (180.0 / Math.PI)) - 90;
        float pitch = position.getPitch(); // TODO


        EntityLookAndRelativeMovePacket entityLookAndRelativeMovePacket = new EntityLookAndRelativeMovePacket();
        entityLookAndRelativeMovePacket.entityId = getEntityId();
        entityLookAndRelativeMovePacket.deltaX = (short) ((newX * 32 - position.getX() * 32) * 128);
        entityLookAndRelativeMovePacket.deltaY = (short) ((newY * 32 - position.getY() * 32) * 128);
        entityLookAndRelativeMovePacket.deltaZ = (short) ((newZ * 32 - position.getZ() * 32) * 128);
        entityLookAndRelativeMovePacket.yaw = yaw;
        entityLookAndRelativeMovePacket.pitch = pitch;
        entityLookAndRelativeMovePacket.onGround = isOnGround();
        sendPacketToViewers(entityLookAndRelativeMovePacket);

        if (lastYaw != yaw) {
            EntityHeadLookPacket entityHeadLookPacket = new EntityHeadLookPacket();
            entityHeadLookPacket.entityId = getEntityId();
            entityHeadLookPacket.yaw = yaw;
            sendPacketToViewers(entityHeadLookPacket);
        }

        refreshPosition(newX, newY, newZ);
        refreshView(yaw, pitch);
    }

    public void moveTowards(Position direction, float speed) {
        float radians = (float) Math.atan2(direction.getZ() - position.getZ(), direction.getX() - position.getX());
        float speedX = (float) (Math.cos(radians) * speed);
        float speedZ = (float) (Math.sin(radians) * speed);
        move(speedX, 0, speedZ);
    }

    @Override
    public void kill() {
        this.isDead = true;
        triggerStatus((byte) 3);
        scheduleRemove(1000);
        DeathEvent deathEvent = new DeathEvent();
        callEvent(DeathEvent.class, deathEvent);
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

    @Override
    public boolean isOnGround() {
        return EntityUtils.isOnGround(this);
    }
}
