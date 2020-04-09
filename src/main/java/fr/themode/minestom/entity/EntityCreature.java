package fr.themode.minestom.entity;

import fr.themode.minestom.entity.pathfinding.EntityPathFinder;
import fr.themode.minestom.entity.property.Attribute;
import fr.themode.minestom.net.packet.server.play.*;
import fr.themode.minestom.net.player.PlayerConnection;
import fr.themode.minestom.utils.BlockPosition;
import fr.themode.minestom.utils.ChunkUtils;
import fr.themode.minestom.utils.Position;
import fr.themode.minestom.utils.Vector;

import java.util.LinkedList;

public abstract class EntityCreature extends LivingEntity {

    private EntityPathFinder pathFinder = new EntityPathFinder(this);
    private LinkedList<BlockPosition> blockPositions;
    private Position targetPosition;

    public EntityCreature(EntityType entityType, Position spawnPosition) {
        super(entityType.getId(), spawnPosition);
    }

    @Override
    public void update() {
        super.update();

        // Path finding
        if (blockPositions != null) {
            if (targetPosition != null) {
                float distance = getPosition().getDistance(targetPosition);
                if (distance < 0.2f) {
                    setNextPathPosition();
                    //System.out.println("END TARGET");
                } else {
                    // TODO jump support
                    moveTowards(targetPosition, getAttributeValue(Attribute.MOVEMENT_SPEED));
                    //System.out.println("MOVE TOWARD " + targetPosition);
                }
            }
        }

    }

    public void move(float x, float y, float z, boolean updateView) {
        Position position = getPosition();
        float newX = position.getX() + x;
        float newY = position.getY() + y;
        float newZ = position.getZ() + z;

        // Creatures cannot move in unload chunk
        if (ChunkUtils.isChunkUnloaded(getInstance(), newX, newZ))
            return;

        float lastYaw = position.getYaw();
        float radians = (float) Math.atan2(newZ - position.getZ(), newX - position.getX());

        float yaw = (float) (radians * (180.0 / Math.PI)) - 90;
        float pitch = position.getPitch(); // TODO

        short deltaX = (short) ((newX * 32 - position.getX() * 32) * 128);
        short deltaY = (short) ((newY * 32 - position.getY() * 32) * 128);
        short deltaZ = (short) ((newZ * 32 - position.getZ() * 32) * 128);

        if (updateView) {
            EntityPositionAndRotationPacket entityPositionAndRotationPacket = new EntityPositionAndRotationPacket();
            entityPositionAndRotationPacket.entityId = getEntityId();
            entityPositionAndRotationPacket.deltaX = deltaX;
            entityPositionAndRotationPacket.deltaY = deltaY;
            entityPositionAndRotationPacket.deltaZ = deltaZ;
            entityPositionAndRotationPacket.yaw = yaw;
            entityPositionAndRotationPacket.pitch = pitch;
            entityPositionAndRotationPacket.onGround = isOnGround();
            sendPacketToViewers(entityPositionAndRotationPacket);
        } else {
            EntityPositionPacket entityPositionPacket = new EntityPositionPacket();
            entityPositionPacket.entityId = getEntityId();
            entityPositionPacket.deltaX = deltaX;
            entityPositionPacket.deltaY = deltaY;
            entityPositionPacket.deltaZ = deltaZ;
            entityPositionPacket.onGround = isOnGround();
            sendPacketToViewers(entityPositionPacket);
        }

        if (lastYaw != yaw) {
            EntityHeadLookPacket entityHeadLookPacket = new EntityHeadLookPacket();
            entityHeadLookPacket.entityId = getEntityId();
            entityHeadLookPacket.yaw = yaw;
            sendPacketToViewers(entityHeadLookPacket);
            refreshView(yaw, pitch);
        }

        refreshPosition(newX, newY, newZ);
    }

    @Override
    public void kill() {
        super.kill();

        // Needed for proper death animation (wait for it to finish before destroying the entity)
        scheduleRemove(1000);
    }

    @Override
    public void addViewer(Player player) {
        super.addViewer(player);
        PlayerConnection playerConnection = player.getPlayerConnection();

        EntityPacket entityPacket = new EntityPacket();
        entityPacket.entityId = getEntityId();

        SpawnLivingEntityPacket spawnLivingEntityPacket = new SpawnLivingEntityPacket();
        spawnLivingEntityPacket.entityId = getEntityId();
        spawnLivingEntityPacket.entityUuid = getUuid();
        spawnLivingEntityPacket.entityType = getEntityType();
        spawnLivingEntityPacket.position = getPosition();
        spawnLivingEntityPacket.headPitch = 0;

        playerConnection.sendPacket(entityPacket);
        playerConnection.sendPacket(spawnLivingEntityPacket);
        playerConnection.sendPacket(getMetadataPacket());
    }

    public void jump(float height) {
        // FIXME magic value
        Vector velocity = new Vector(0, height * 6, 0);
        setVelocity(velocity, 250);
    }

    public void moveTo(Position position) {
        pathFinder.getPath(position, blockPositions -> {
            this.blockPositions = blockPositions;
            setNextPathPosition();
        });
    }

    public void moveTowards(Position direction, float speed) {
        float radians = (float) Math.atan2(direction.getZ() - position.getZ(), direction.getX() - position.getX());
        float speedX = (float) (Math.cos(radians) * speed);
        float speedZ = (float) (Math.sin(radians) * speed);
        move(speedX, 0, speedZ, true);
    }

    private void setNextPathPosition() {
        BlockPosition blockPosition = blockPositions.pollFirst();

        if (blockPosition == null) {
            this.blockPositions = null;
            this.targetPosition = null;
            return;
        }

        this.targetPosition = blockPosition.toPosition().subtract(0.5f, 0, 0.5f);
    }
}
