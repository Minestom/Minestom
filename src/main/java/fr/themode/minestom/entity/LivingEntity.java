package fr.themode.minestom.entity;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.event.PickupItemEvent;
import fr.themode.minestom.instance.Chunk;
import fr.themode.minestom.item.ItemStack;
import fr.themode.minestom.net.packet.server.play.CollectItemPacket;

import java.util.Set;

// TODO attributes https://wiki.vg/Protocol#Entity_Properties
public abstract class LivingEntity extends Entity {

    protected boolean canPickupItem;
    protected boolean isDead;

    private boolean isHandActive;
    private boolean activeHand;
    private boolean riptideSpinAttack;

    public LivingEntity(int entityType) {
        super(entityType);
    }

    public abstract void kill();

    @Override
    public void update() {
        if (canPickupItem) {
            Chunk chunk = instance.getChunkAt(getPosition()); // TODO check surrounding chunks
            Set<ObjectEntity> objectEntities = chunk.getObjectEntities();
            for (ObjectEntity objectEntity : objectEntities) {
                if (objectEntity instanceof ItemEntity) {
                    ItemEntity itemEntity = (ItemEntity) objectEntity;
                    if (!itemEntity.isPickable())
                        continue;
                    float distance = getDistance(objectEntity);
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
                                objectEntity.remove();
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
}
