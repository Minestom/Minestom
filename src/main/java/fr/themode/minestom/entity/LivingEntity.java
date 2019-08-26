package fr.themode.minestom.entity;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.entity.property.Attribute;
import fr.themode.minestom.event.PickupItemEvent;
import fr.themode.minestom.instance.Chunk;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.net.packet.server.play.CollectItemPacket;
import fr.themode.minestom.net.packet.server.play.EntityPropertiesPacket;

import java.util.Set;

public abstract class LivingEntity extends Entity {

    protected boolean canPickupItem;
    protected boolean isDead;

    private float health;

    private float[] attributeValues = new float[Attribute.values().length];

    private boolean isHandActive;
    private boolean activeHand;
    private boolean riptideSpinAttack;

    public LivingEntity(int entityType) {
        super(entityType);
        setupAttributes();
    }

    public abstract void kill();

    @Override
    public void update() {
        if (canPickupItem) {
            Chunk chunk = instance.getChunkAt(getPosition()); // TODO check surrounding chunks
            Set<Entity> entities = instance.getChunkEntities(chunk);
            for (Entity entity : entities) {
                if (entity instanceof ItemEntity) {
                    ItemEntity itemEntity = (ItemEntity) entity;
                    if (!itemEntity.isPickable())
                        continue;
                    float distance = getDistance(entity);
                    if (distance <= 2.04) {
                        synchronized (itemEntity) {
                            if (itemEntity.shouldRemove())
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
    public Buffer getMetadataBuffer() {
        Buffer buffer = super.getMetadataBuffer();
        buffer.putByte((byte) 7);
        buffer.putByte(METADATA_BYTE);
        byte activeHandValue = 0;
        if (isHandActive) {
            activeHandValue += 1;
            if (activeHand)
                activeHandValue += 2;
            if (riptideSpinAttack)
                activeHandValue += 4;
        }
        buffer.putByte(activeHandValue);
        return buffer;
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
        if (this.health <= 0) {
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
            property.key = attribute.getKey();
            property.value = getAttributeValue(attribute);
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
