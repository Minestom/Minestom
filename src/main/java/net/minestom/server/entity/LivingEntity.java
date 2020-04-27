package net.minestom.server.entity;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.property.Attribute;
import net.minestom.server.event.DeathEvent;
import net.minestom.server.event.EntityDamageEvent;
import net.minestom.server.event.PickupItemEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.play.CollectItemPacket;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket;
import net.minestom.server.network.packet.server.play.EntityPropertiesPacket;
import net.minestom.server.utils.Position;

import java.util.Set;
import java.util.function.Consumer;

public abstract class LivingEntity extends Entity {

    protected boolean canPickupItem;
    protected boolean isDead;

    private float health;

    // Bounding box used for items' pickup (see LivingEntity#setBoundingBox)
    private BoundingBox expandedBoundingBox;

    private float[] attributeValues = new float[Attribute.values().length];

    private boolean isHandActive;
    private boolean offHand;
    private boolean riptideSpinAttack;

    public LivingEntity(int entityType, Position spawnPosition) {
        super(entityType, spawnPosition);
        setupAttributes();
        setGravity(0.02f);
    }

    public LivingEntity(int entityType) {
        this(entityType, new Position());
    }

    @Override
    public void update() {
        // Items picking
        if (canPickupItem) {
            Chunk chunk = instance.getChunkAt(getPosition()); // TODO check surrounding chunks
            Set<Entity> entities = instance.getChunkEntities(chunk);
            BoundingBox livingBoundingBox = expandedBoundingBox;
            for (Entity entity : entities) {
                if (entity instanceof ItemEntity) {
                    ItemEntity itemEntity = (ItemEntity) entity;
                    if (!itemEntity.isPickable())
                        continue;
                    BoundingBox itemBoundingBox = itemEntity.getBoundingBox();
                    if (livingBoundingBox.intersect(itemBoundingBox)) {
                        synchronized (itemEntity) {
                            if (itemEntity.shouldRemove() || itemEntity.isRemoveScheduled())
                                continue;
                            ItemStack item = itemEntity.getItemStack();
                            PickupItemEvent pickupItemEvent = new PickupItemEvent(item);
                            callCancellableEvent(PickupItemEvent.class, pickupItemEvent, () -> {
                                CollectItemPacket collectItemPacket = new CollectItemPacket();
                                collectItemPacket.collectedEntityId = itemEntity.getEntityId();
                                collectItemPacket.collectorEntityId = getEntityId();
                                collectItemPacket.pickupItemCount = item.getAmount();
                                sendPacketToViewersAndSelf(collectItemPacket);
                                entity.remove();
                            });
                        }
                    }
                }
            }
        }
    }

    @Override
    public Consumer<PacketWriter> getMetadataConsumer() {
        return packet -> {
            super.getMetadataConsumer().accept(packet);
            packet.writeByte((byte) 7);
            packet.writeByte(METADATA_BYTE);
            byte activeHandValue = 0;
            if (isHandActive) {
                activeHandValue += 1;
                if (offHand)
                    activeHandValue += 2;
                if (riptideSpinAttack)
                    activeHandValue += 4;
            }
            packet.writeByte(activeHandValue);

            // TODO all remaining metadata
        };
    }

    public void kill() {
        refreshIsDead(true); // So the entity isn't killed over and over again
        triggerStatus((byte) 3); // Start death animation status
        setHealth(0);
        DeathEvent deathEvent = new DeathEvent();
        callEvent(DeathEvent.class, deathEvent);
    }

    /**
     * @param type  the damage type
     * @param value the amount of damage
     * @return true if damage has been applied, false if it didn't
     */
    public boolean damage(DamageType type, float value) {
        if (isImmune(type)) {
            return false;
        }

        EntityDamageEvent entityDamageEvent = new EntityDamageEvent(type, value);
        callCancellableEvent(EntityDamageEvent.class, entityDamageEvent, () -> {
            float damage = entityDamageEvent.getDamage();

            EntityAnimationPacket entityAnimationPacket = new EntityAnimationPacket();
            entityAnimationPacket.entityId = getEntityId();
            entityAnimationPacket.animation = EntityAnimationPacket.Animation.TAKE_DAMAGE;
            sendPacketToViewersAndSelf(entityAnimationPacket);
            setHealth(getHealth() - damage);
        });

        return !entityDamageEvent.isCancelled();
    }

    /**
     * Is this entity immune to the given type of damage?
     *
     * @param type the type of damage
     * @return true iff this entity is immune to the given type of damage
     */
    public boolean isImmune(DamageType type) {
        return false;
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        health = Math.min(health, getMaxHealth());

        this.health = health;
        if (this.health <= 0 && !isDead) {
            kill();
        }
    }

    public float getMaxHealth() {
        return getAttributeValue(Attribute.MAX_HEALTH);
    }

    public void heal() {
        setHealth(getAttributeValue(Attribute.MAX_HEALTH));
    }

    public void setAttribute(Attribute attribute, float value) {
        this.attributeValues[attribute.ordinal()] = value;
    }

    public float getAttributeValue(Attribute attribute) {
        return this.attributeValues[attribute.ordinal()];
    }

    public boolean isDead() {
        return isDead;
    }

    public boolean canPickupItem() {
        return canPickupItem;
    }

    public void setCanPickupItem(boolean canPickupItem) {
        this.canPickupItem = canPickupItem;
    }

    @Override
    public void setBoundingBox(float x, float y, float z) {
        super.setBoundingBox(x, y, z);
        this.expandedBoundingBox = getBoundingBox().expand(1, 0.5f, 1);
    }

    public void refreshActiveHand(boolean isHandActive, boolean offHand, boolean riptideSpinAttack) {
        this.isHandActive = isHandActive;
        this.offHand = offHand;
        this.riptideSpinAttack = riptideSpinAttack;
    }

    public void refreshIsDead(boolean isDead) {
        this.isDead = isDead;
    }

    protected EntityPropertiesPacket getPropertiesPacket() {
        EntityPropertiesPacket propertiesPacket = new EntityPropertiesPacket();
        propertiesPacket.entityId = getEntityId();

        int length = Attribute.values().length;
        EntityPropertiesPacket.Property[] properties = new EntityPropertiesPacket.Property[length];
        for (int i = 0; i < length; i++) {
            Attribute attribute = Attribute.values()[i];
            EntityPropertiesPacket.Property property = new EntityPropertiesPacket.Property();
            float maxValue = attribute.getMaxVanillaValue();
            float value = getAttributeValue(attribute);
            value = value > maxValue ? maxValue : value; // Bypass vanilla limit client-side if needed (by sending the max value allowed)

            property.key = attribute.getKey();
            property.value = value;
            properties[i] = property;
        }

        propertiesPacket.properties = properties;
        return propertiesPacket;
    }

    private void setupAttributes() {
        for (Attribute attribute : Attribute.values()) {
            setAttribute(attribute, attribute.getDefaultValue());
        }
    }

    @Override
    protected void handleVoid() {
        // Kill if in void
        if (getInstance().isInVoid(this.position)) {
            damage(DamageType.VOID, 10f);
        }
    }
}
