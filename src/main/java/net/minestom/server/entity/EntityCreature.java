package net.minestom.server.entity;

import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.entity.pathfinding.EntityPathFinder;
import net.minestom.server.entity.property.Attribute;
import net.minestom.server.event.ArmorEquipEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.ChunkUtils;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;

import java.util.LinkedList;
import java.util.function.Consumer;

public abstract class EntityCreature extends LivingEntity {

    private EntityPathFinder pathFinder = new EntityPathFinder(this);
    private LinkedList<BlockPosition> blockPositions;
    private Position targetPosition;

    // Equipments
    private ItemStack mainHandItem;
    private ItemStack offHandItem;

    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack leggings;
    private ItemStack boots;


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
                //System.out.println("test: "+distance);
                if (distance < 0.7f) {
                    setNextPathPosition();
                    //System.out.println("END TARGET");
                } else {
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
        Position newPosition = new Position();
        // Calculate collisions boxes
        onGround = CollisionUtils.handlePhysics(this, new Vector(x, y, z), newPosition, new Vector());
        // Refresh target position
        newX = newPosition.getX();
        newY = newPosition.getY();
        newZ = newPosition.getZ();

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

    @Override
    public ItemStack getItemInMainHand() {
        return mainHandItem;
    }

    @Override
    public void setItemInMainHand(ItemStack itemStack) {
        this.mainHandItem = itemStack;
        syncEquipment(EntityEquipmentPacket.Slot.MAIN_HAND);
    }

    @Override
    public ItemStack getItemInOffHand() {
        return offHandItem;
    }

    @Override
    public void setItemInOffHand(ItemStack itemStack) {
        this.offHandItem = itemStack;
        syncEquipment(EntityEquipmentPacket.Slot.OFF_HAND);
    }

    @Override
    public ItemStack getHelmet() {
        return helmet;
    }

    @Override
    public void setHelmet(ItemStack itemStack) {
        this.helmet = getEquipmentItem(itemStack, ArmorEquipEvent.ArmorSlot.HELMET);
        syncEquipment(EntityEquipmentPacket.Slot.HELMET);
    }

    @Override
    public ItemStack getChestplate() {
        return chestplate;
    }

    @Override
    public void setChestplate(ItemStack itemStack) {
        this.chestplate = getEquipmentItem(itemStack, ArmorEquipEvent.ArmorSlot.CHESTPLATE);
        syncEquipment(EntityEquipmentPacket.Slot.CHESTPLATE);
    }

    @Override
    public ItemStack getLeggings() {
        return leggings;
    }

    @Override
    public void setLeggings(ItemStack itemStack) {
        this.leggings = getEquipmentItem(itemStack, ArmorEquipEvent.ArmorSlot.LEGGINGS);
        syncEquipment(EntityEquipmentPacket.Slot.LEGGINGS);
    }

    @Override
    public ItemStack getBoots() {
        return boots;
    }

    @Override
    public void setBoots(ItemStack itemStack) {
        this.boots = getEquipmentItem(itemStack, ArmorEquipEvent.ArmorSlot.BOOTS);
        syncEquipment(EntityEquipmentPacket.Slot.BOOTS);
    }

    public void jump(float height) {
        // FIXME magic value
        Vector velocity = new Vector(0, height * 10, 0);
        setVelocity(velocity);
    }

    public void setPathTo(Position position, int maxCheck, Consumer<Boolean> resultConsumer) {
        pathFinder.getPath(position, maxCheck, blockPositions -> {
            if (blockPositions == null || blockPositions.isEmpty()) {
                // Didn't find path
                if (resultConsumer != null)
                    resultConsumer.accept(false);
                return;
            }
            blockPositions.pollFirst(); // Remove first entry (where the entity is)

            this.blockPositions = blockPositions;
            setNextPathPosition();

            if (resultConsumer != null)
                resultConsumer.accept(true);
        });
    }

    public void setPathTo(Position position, int maxCheck) {
        setPathTo(position, maxCheck, null);
    }

    public void setPathTo(Position position) {
        setPathTo(position, 1000, null);
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

        this.targetPosition = blockPosition.toPosition();//.add(0.5f, 0, 0.5f);
        // FIXME: jump support
        if (blockPosition.getY() > getPosition().getY())
            jump(1);
    }

    private ItemStack getEquipmentItem(ItemStack itemStack, ArmorEquipEvent.ArmorSlot armorSlot) {
        ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(itemStack, armorSlot);
        callEvent(ArmorEquipEvent.class, armorEquipEvent);
        return armorEquipEvent.getArmorItem();
    }
}
