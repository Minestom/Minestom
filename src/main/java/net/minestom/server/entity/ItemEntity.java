package net.minestom.server.entity;

import net.minestom.server.event.entity.EntityItemMergeEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.StackingRule;
import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.utils.Position;

import java.util.Set;
import java.util.function.Consumer;

public class ItemEntity extends ObjectEntity {

    private ItemStack itemStack;

    private boolean pickable = true;
    private boolean mergeable = true;

    private long spawnTime;
    private long pickupDelay;

    public ItemEntity(ItemStack itemStack, Position spawnPosition) {
        super(EntityType.ITEM, spawnPosition);
        this.itemStack = itemStack;
        setBoundingBox(0.25f, 0.25f, 0.25f);
        setGravity(0.025f);
    }

    @Override
    public void update() {
        if (isMergeable() && isPickable()) {
            Chunk chunk = instance.getChunkAt(getPosition());
            Set<Entity> entities = instance.getChunkEntities(chunk);
            for (Entity entity : entities) {
                if (entity instanceof ItemEntity) {

                    // Do not merge with itself
                    if (entity == this)
                        continue;

                    ItemEntity itemEntity = (ItemEntity) entity;
                    if (!itemEntity.isPickable() || !itemEntity.isMergeable())
                        continue;

                    // Too far, do not merge
                    if (getDistance(itemEntity) > 1)
                        continue;

                    synchronized (this) {
                        synchronized (itemEntity) {
                            ItemStack itemStackEntity = itemEntity.getItemStack();

                            StackingRule stackingRule = itemStack.getStackingRule();
                            boolean canStack = stackingRule.canBeStacked(itemStack, itemStackEntity);

                            if (!canStack)
                                continue;

                            int totalAmount = stackingRule.getAmount(itemStack) + stackingRule.getAmount(itemStackEntity);
                            boolean canApply = stackingRule.canApply(itemStack, totalAmount);

                            if (!canApply)
                                continue;

                            EntityItemMergeEvent entityItemMergeEvent = new EntityItemMergeEvent(this, itemEntity);
                            callCancellableEvent(EntityItemMergeEvent.class, entityItemMergeEvent, () -> {
                                ItemStack result = stackingRule.apply(itemStack.clone(), totalAmount);
                                setItemStack(result);
                                itemEntity.remove();
                            });

                        }
                    }

                }
            }
        }
    }

    @Override
    public void spawn() {
        this.spawnTime = System.currentTimeMillis();
    }

    @Override
    public void addViewer(Player player) {
        super.addViewer(player);
    }

    @Override
    public Consumer<PacketWriter> getMetadataConsumer() {
        return packet -> {
            super.getMetadataConsumer().accept(packet);
            fillMetadataIndex(packet, 7);
        };
    }

    @Override
    protected void fillMetadataIndex(PacketWriter packet, int index) {
        super.fillMetadataIndex(packet, index);
        if (index == 7) {
            packet.writeByte((byte) 7);
            packet.writeByte(METADATA_SLOT);
            packet.writeItemStack(itemStack);
        }

    }

    @Override
    public int getObjectData() {
        return 1;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
        sendMetadataIndex(7); // Refresh itemstack for viewers
    }

    public boolean isPickable() {
        return pickable && (System.currentTimeMillis() - getSpawnTime() >= pickupDelay);
    }

    public void setPickable(boolean pickable) {
        this.pickable = pickable;
    }

    public boolean isMergeable() {
        return mergeable;
    }

    public void setMergeable(boolean mergeable) {
        this.mergeable = mergeable;
    }

    public long getPickupDelay() {
        return pickupDelay;
    }

    public void setPickupDelay(long pickupDelay) {
        this.pickupDelay = pickupDelay;
    }

    public long getSpawnTime() {
        return spawnTime;
    }
}
