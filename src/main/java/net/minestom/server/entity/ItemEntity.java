package net.minestom.server.entity;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.event.entity.EntityItemMergeEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.StackingRule;
import net.minestom.server.network.packet.server.play.CollectItemPacket;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.time.CooldownUtils;
import net.minestom.server.utils.time.TimeUnit;
import net.minestom.server.utils.time.UpdateOption;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Represents an item on the ground.
 */
public class ItemEntity extends ObjectEntity {

    /**
     * Used to slow down the merge check delay
     */
    private static UpdateOption mergeUpdateOption = new UpdateOption(10, TimeUnit.TICK);

    /**
     * The last time that this item has checked his neighbors for merge
     */
    private long lastMergeCheck;

    private ItemStack itemStack;

    private boolean pickable = true;
    private boolean mergeable = true;
    private float mergeRange = 1;
    private float pickupRange = 1.5f;

    private long spawnTime;
    private long pickupDelay;

    /**
     * Creates an ItemEntity with a type as well.
     *
     * @param entityType The type of entity you want to pick up.
     * @param itemStack The ItemStack that this entity holds
     * @param spawnPosition Where to spawn this entities.
     */
    public ItemEntity(@NotNull EntityType entityType, @NotNull ItemStack itemStack, @NotNull Position spawnPosition) {
        super(entityType, spawnPosition);
        setItemStack(itemStack);
        setBoundingBox(0.25f, 0.25f, 0.25f);
    }

    /**
     * Creates an ItemEntity with a specific type and position
     *
     * @param itemStack The ItemStack that this entity holds
     * @param spawnPosition Where to spawn this entities.
     */
    public ItemEntity(@NotNull ItemStack itemStack, @NotNull Position spawnPosition) {
        this(EntityType.ITEM, itemStack, spawnPosition);
    }

    /**
     * Creates an ItemEntity with a type as well. Convenience constructor to set the instance as well.
     *
     * @param entityType The type of entity you want to pick up.
     * @param itemStack The ItemStack that this entity holds
     * @param spawnPosition Where to spawn this entities.
     */
    public ItemEntity(@NotNull EntityType entityType, @NotNull ItemStack itemStack, @NotNull Position spawnPosition, @Nullable Instance instance) {
        this(entityType, itemStack, spawnPosition);

        if (instance != null) {
            setInstance(instance);
        }
    }

    /**
     * Creates an ItemEntity with a specific item, position, and instance. Convenience constructor to set the instance as well.
     *
     * @param itemStack The ItemStack that this entity holds
     * @param spawnPosition Where to spawn this entities.
     * @param instance The instance to set this entity at.
     */
    public ItemEntity(@NotNull ItemStack itemStack, @NotNull Position spawnPosition, @Nullable Instance instance) {
        this(EntityType.ITEM, itemStack, spawnPosition, instance);
    }

    /**
     * Gets the update option for the merging feature.
     *
     * @return the merge update option
     */
    @Nullable
    public static UpdateOption getMergeUpdateOption() {
        return mergeUpdateOption;
    }

    /**
     * Changes the merge update option.
     * Can be set to null to entirely remove the delay.
     *
     * @param mergeUpdateOption the new merge update option
     */
    public static void setMergeUpdateOption(@Nullable UpdateOption mergeUpdateOption) {
        ItemEntity.mergeUpdateOption = mergeUpdateOption;
    }

    @Override
    public void update(long time) {
        // TODO add pickup check cooldown?
        if (isMergeable() && isPickable() &&
                (mergeUpdateOption == null || !CooldownUtils.hasCooldown(time, lastMergeCheck, mergeUpdateOption))) {
            this.lastMergeCheck = time;

            final Chunk chunk = instance.getChunkAt(getPosition());
            final Set<Entity> entities = instance.getChunkEntities(chunk);
            for (Entity entity : entities) {
                if (entity instanceof ItemEntity) {

                    // Do not merge with itself
                    if (entity == this)
                        continue;

                    final ItemEntity itemEntity = (ItemEntity) entity;
                    if (!itemEntity.isPickable() || !itemEntity.isMergeable())
                        continue;

                    // Too far, do not merge
                    if (getDistance(itemEntity) > mergeRange)
                        continue;

                    final ItemStack itemStackEntity = itemEntity.getItemStack();

                    final StackingRule stackingRule = itemStack.getStackingRule();
                    final boolean canStack = stackingRule.canBeStacked(itemStack, itemStackEntity);

                    if (!canStack)
                        continue;

                    final int totalAmount = stackingRule.getAmount(itemStack) + stackingRule.getAmount(itemStackEntity);
                    final boolean canApply = stackingRule.canApply(itemStack, totalAmount);

                    if (!canApply)
                        continue;

                    final ItemStack result = stackingRule.apply(itemStack.clone(), totalAmount);

                    EntityItemMergeEvent entityItemMergeEvent = new EntityItemMergeEvent(this, itemEntity, result);
                    callCancellableEvent(EntityItemMergeEvent.class, entityItemMergeEvent, () -> {
                        setItemStack(entityItemMergeEvent.getResult());
                        itemEntity.remove();
                    });

                }
                else if (entity instanceof LivingEntity) {

                    // Do not pickup if not visible
                    if (entity instanceof Player && !this.isViewer((Player) entity))
                        continue;

                    // Do not pick up if the entity can not pick up items
                    final LivingEntity livingEntity = (LivingEntity) entity;
                    if (!livingEntity.canPickupItem())
                        continue;

                    // Do not pick up the item if its too far away.
                    if (getDistance(entity) > pickupRange)
                        continue;

                    // Do not pick up if the entity's "expanded bounding box" doesn't intersect with this item.
                    final BoundingBox itemBoundingBox = this.getBoundingBox();
                    if (livingEntity.getExpandedBoundingBox().intersect(itemBoundingBox)) {
                        if (this.isRemoveScheduled())
                            continue;
                        final ItemStack item = this.getItemStack();
                        PickupItemEvent pickupItemEvent = new PickupItemEvent(livingEntity, item);
                        callCancellableEvent(PickupItemEvent.class, pickupItemEvent, () -> {
                            CollectItemPacket collectItemPacket = new CollectItemPacket();
                            collectItemPacket.collectedEntityId = this.getEntityId();
                            collectItemPacket.collectorEntityId = getEntityId();
                            collectItemPacket.pickupItemCount = item.getAmount();
                            sendPacketToViewersAndSelf(collectItemPacket);
                            this.remove();
                        });
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
    public int getObjectData() {
        return 1;
    }

    /**
     * Gets the item stack on ground.
     *
     * @return the item stack
     */
    @NotNull
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * Changes the item stack on ground.
     *
     * @param itemStack the item stack
     */
    public void setItemStack(@NotNull ItemStack itemStack) {
        this.itemStack = itemStack;
        this.metadata.setIndex((byte) 7, Metadata.Slot(itemStack));
    }

    /**
     * Gets if the item is currently pickable.
     * <p>
     * {@link #setPickable(boolean)} needs to be true and the delay {@link #getPickupDelay()}
     * to be long gone.
     *
     * @return true if the item is pickable, false otherwise
     */
    public boolean isPickable() {
        return pickable && (System.currentTimeMillis() - getSpawnTime() >= pickupDelay);
    }

    /**
     * Makes the item pickable.
     *
     * @param pickable true to make the item pickable, false otherwise
     */
    public void setPickable(boolean pickable) {
        this.pickable = pickable;
    }

    /**
     * Gets if the item is mergeable.
     *
     * @return true if the entity is mergeable, false otherwise
     */
    public boolean isMergeable() {
        return mergeable;
    }

    /**
     * When set to true, close {@link ItemEntity} will try to merge together as a single entity
     * when their {@link #getItemStack()} is similar and allowed to stack together.
     *
     * @param mergeable should the entity merge with other {@link ItemEntity}
     */
    public void setMergeable(boolean mergeable) {
        this.mergeable = mergeable;
    }

    /**
     * Gets the merge range.
     *
     * @return the merge range
     */
    public float getMergeRange() {
        return mergeRange;
    }

    /**
     * Changes the merge range.
     *
     * @param mergeRange the merge range
     */
    public void setMergeRange(float mergeRange) {
        this.mergeRange = mergeRange;
    }

    /**
     * Gets the pickup range.
     *
     * @return the pickup range
     */
    public float getPickupRange() {
        return pickupRange;
    }

    /**
     * Changes the pickup range
     *
     * @param pickupRange the pickup range
     */
    public void setPickupRange(float pickupRange) {
        this.pickupRange = pickupRange;
    }

    /**
     * Gets the pickup delay in milliseconds, defined by {@link #setPickupDelay(long, TimeUnit)}.
     *
     * @return the pickup delay
     */
    public long getPickupDelay() {
        return pickupDelay;
    }

    /**
     * Sets the pickup delay of the ItemEntity.
     *
     * @param delay    the pickup delay
     * @param timeUnit the unit of the delay
     */
    public void setPickupDelay(long delay, @NotNull TimeUnit timeUnit) {
        this.pickupDelay = timeUnit.toMilliseconds(delay);
    }

    /**
     * Used to know if the ItemEntity can be pickup.
     *
     * @return the time in milliseconds since this entity has spawn
     */
    public long getSpawnTime() {
        return spawnTime;
    }
}
