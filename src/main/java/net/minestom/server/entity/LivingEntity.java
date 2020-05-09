package net.minestom.server.entity;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.property.Attribute;
import net.minestom.server.event.entity.DeathEvent;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.entity.EntityFireEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.inventory.EquipmentHandler;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.sound.Sound;
import net.minestom.server.sound.SoundCategory;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.time.TimeUnit;

import java.util.Set;
import java.util.function.Consumer;

public abstract class LivingEntity extends Entity implements EquipmentHandler {

    protected boolean canPickupItem;
    protected boolean isDead;

    private float health;

    // Bounding box used for items' pickup (see LivingEntity#setBoundingBox)
    private BoundingBox expandedBoundingBox;

    private float[] attributeValues = new float[Attribute.values().length];

    private boolean isHandActive;
    private boolean offHand;
    private boolean riptideSpinAttack;

    /**
     * Time at which this entity must be extinguished
     */
    private long fireExtinguishTime;

    /**
     * Last time the fire damage was applied
     */
    private long lastFireDamageTime;

    /**
     * Period, in ms, between two fire damage applications
     */
    private long fireDamagePeriod = 1000L;

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
        if (isOnFire()) {
            if (System.currentTimeMillis() > fireExtinguishTime) {
                setOnFire(false);
            } else {
                if (System.currentTimeMillis() - lastFireDamageTime > fireDamagePeriod) {
                    damage(DamageType.ON_FIRE, 1.0f);
                    lastFireDamageTime = System.currentTimeMillis();
                }
            }
        }

        // Items picking
        if (canPickupItem()) {
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
            fillMetadataIndex(packet, 7);

            // TODO all remaining metadata
        };
    }

    @Override
    protected void fillMetadataIndex(PacketWriter packet, int index) {
        super.fillMetadataIndex(packet, index);
        if (index == 7) {
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
        }
    }

    @Override
    public void addViewer(Player player) {
        super.addViewer(player);

        // Equipments synchronization
        syncEquipments();
    }

    public void kill() {
        refreshIsDead(true); // So the entity isn't killed over and over again
        triggerStatus((byte) 3); // Start death animation status
        setHealth(0);
        DeathEvent deathEvent = new DeathEvent();
        callEvent(DeathEvent.class, deathEvent);
    }

    /**
     * Sets fire to this entity for a given duration
     *
     * @param duration duration in ticks of the effect
     */
    public void setFireForDuration(int duration) {
        setFireForDuration(duration, TimeUnit.TICK);
    }

    /**
     * Sets fire to this entity for a given duration
     *
     * @param duration duration of the effet
     * @param unit     unit used to express the duration
     */
    public void setFireForDuration(int duration, TimeUnit unit) {
        EntityFireEvent entityFireEvent = new EntityFireEvent(duration, unit);
        callCancellableEvent(EntityFireEvent.class, entityFireEvent, () -> {
            long fireTime = entityFireEvent.getFireTime(TimeUnit.MILLISECOND);
            setOnFire(true);
            fireExtinguishTime = System.currentTimeMillis() + fireTime;
        });
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

            // play damage sound
            Sound sound = type.getSound(this);
            if (sound != null) {
                SoundCategory soundCategory;
                if (this instanceof Player) {
                    soundCategory = SoundCategory.PLAYERS;
                } else {
                    // TODO: separate living entity categories
                    soundCategory = SoundCategory.HOSTILE;
                }

                SoundEffectPacket damageSoundPacket = SoundEffectPacket.create(soundCategory, sound, getPosition().getX(), getPosition().getY(), getPosition().getZ(), 1.0f, 1.0f);
                sendPacketToViewersAndSelf(damageSoundPacket);
            }
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

    // Equipments
    public void syncEquipments() {
        for (EntityEquipmentPacket.Slot slot : EntityEquipmentPacket.Slot.values()) {
            syncEquipment(slot);
        }
    }

    public void syncEquipment(EntityEquipmentPacket.Slot slot) {
        EntityEquipmentPacket entityEquipmentPacket = getEquipmentPacket(slot);
        if (entityEquipmentPacket == null)
            return;

        sendPacketToViewers(entityEquipmentPacket);
    }

    protected EntityEquipmentPacket getEquipmentPacket(EntityEquipmentPacket.Slot slot) {
        ItemStack itemStack = getEquipment(slot);
        if (itemStack == null)
            return null;

        EntityEquipmentPacket equipmentPacket = new EntityEquipmentPacket();
        equipmentPacket.entityId = getEntityId();
        equipmentPacket.slot = slot;
        equipmentPacket.itemStack = itemStack;
        return equipmentPacket;
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

    public long getFireDamagePeriod() {
        return fireDamagePeriod;
    }

    public void setFireDamagePeriod(long fireDamagePeriod) {
        this.fireDamagePeriod = fireDamagePeriod;
    }
}
