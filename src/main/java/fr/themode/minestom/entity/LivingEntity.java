package fr.themode.minestom.entity;

import com.github.simplenet.packet.Packet;
import fr.themode.minestom.collision.BoundingBox;
import fr.themode.minestom.entity.property.Attribute;
import fr.themode.minestom.event.DeathEvent;
import fr.themode.minestom.event.PickupItemEvent;
import fr.themode.minestom.instance.Chunk;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.net.packet.server.play.CollectItemPacket;
import fr.themode.minestom.net.packet.server.play.EntityAnimationPacket;
import fr.themode.minestom.net.packet.server.play.EntityPropertiesPacket;
import fr.themode.minestom.utils.Position;

import java.util.Set;
import java.util.function.Consumer;

public abstract class LivingEntity extends Entity {

    protected boolean canPickupItem;
    protected boolean isDead;

    private float health;

    // Bounding box used for items' pickup (see LivingEntity#setBoundingBox)
    private BoundingBox expandedBoundingBox = getBoundingBox();

    private float[] attributeValues = new float[Attribute.values().length];

    private boolean isHandActive;
    private boolean activeHand;
    private boolean riptideSpinAttack;

    public LivingEntity(int entityType, Position defaultPosition) {
        super(entityType, defaultPosition);
        setupAttributes();
        setGravity(0.02f);
    }

    public LivingEntity(int entityType) {
        this(entityType, new Position());
    }

    public void kill() {
        System.out.println("KILL");
        this.isDead = true; // So the entity isn't killed over and over again
        setHealth(0);
        triggerStatus((byte) 3); // Start death animation status
        DeathEvent deathEvent = new DeathEvent();
        callEvent(DeathEvent.class, deathEvent);
    }

    @Override
    public void update() {
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
    public Consumer<Packet> getMetadataConsumer() {
        return packet -> {
            super.getMetadataConsumer().accept(packet);
            packet.putByte((byte) 7);
            packet.putByte(METADATA_BYTE);
            byte activeHandValue = 0;
            if (isHandActive) {
                activeHandValue += 1;
                if (activeHand)
                    activeHandValue += 2;
                if (riptideSpinAttack)
                    activeHandValue += 4;
            }
            packet.putByte(activeHandValue);

            // TODO all remaining metadata
        };
    }

    public void damage(float value) {
        EntityAnimationPacket entityAnimationPacket = new EntityAnimationPacket();
        entityAnimationPacket.entityId = getEntityId();
        entityAnimationPacket.animation = EntityAnimationPacket.Animation.TAKE_DAMAGE;
        sendPacketToViewersAndSelf(entityAnimationPacket);
        setHealth(getHealth() - value);
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
        this.activeHand = offHand;
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
}
