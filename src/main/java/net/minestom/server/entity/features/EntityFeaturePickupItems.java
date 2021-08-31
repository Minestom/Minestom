package net.minestom.server.entity.features;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.ExperienceOrb;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.item.PickupExperienceEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.CollectItemPacket;
import net.minestom.server.utils.time.Cooldown;
import net.minestom.server.utils.time.TimeUnit;

import java.time.Duration;
import java.util.Set;

public class EntityFeaturePickupItems extends EntityFeatureBase {

    // ItemStack pickup
    protected boolean canPickupItem = true;
    protected Cooldown itemPickupCooldown = new Cooldown(Duration.of(5, TimeUnit.SERVER_TICK));

    // Experience orb pickup
    protected boolean canPickupExperience;
    protected Cooldown experiencePickupCooldown = new Cooldown(Duration.of(10, TimeUnit.SERVER_TICK));

    // Bounding box used for items' pickup (see LivingEntity#setBoundingBox)
    protected BoundingBox expandedBoundingBox;

    public EntityFeaturePickupItems(Entity entity) {
        super(entity);
        onBoundingBoxUpdated();
    }

    @Override
    public void tick(long time) {
        boolean canPickupItem = canPickupItem() && itemPickupCooldown.isReady(time);
        boolean canPickupExperience = canPickupExperience() && experiencePickupCooldown.isReady(time);
        if (!canPickupItem && !canPickupExperience) {
            return;
        }
        if (canPickupItem) {
            itemPickupCooldown.refreshLastUpdate(time);
        }
        if (canPickupExperience) {
            experiencePickupCooldown.refreshLastUpdate(time);
        }
        Chunk chunk = entity.getChunk();
        Set<Entity> entities = entity.getInstance().getChunkEntities(chunk);
        for (Entity ent : entities) {
            if (canPickupItem && ent instanceof ItemEntity) {
                if (entity instanceof Player && !ent.isViewer((Player) entity)) {
                    continue;
                }
                ItemEntity itemEntity = (ItemEntity) ent;
                if (!itemEntity.isPickable()) {
                    continue;
                }
                BoundingBox itemBoundingBox = itemEntity.getBoundingBox();
                if (expandedBoundingBox.intersect(itemBoundingBox)) {
                    if (itemEntity.shouldRemove() || itemEntity.isRemoveScheduled()) {
                        continue;
                    }
                    PickupItemEvent pickupItemEvent = new PickupItemEvent(entity, itemEntity);
                    EventDispatcher.callCancellable(pickupItemEvent, () -> {
                        final ItemStack is = itemEntity.getItemStack();
                        entity.sendPacketToViewersAndSelf(new CollectItemPacket(itemEntity.getEntityId(), entity.getEntityId(), is.getAmount()));
                        itemEntity.remove();
                    });
                }
            } else if (canPickupExperience && ent instanceof ExperienceOrb) {
                if (entity instanceof Player && !ent.isViewer((Player) entity)) {
                    continue;
                }
                ExperienceOrb experienceOrb = (ExperienceOrb) ent;
                BoundingBox orbBoundingBox = experienceOrb.getBoundingBox();
                if (expandedBoundingBox.intersect(orbBoundingBox)) {
                    if (experienceOrb.shouldRemove() || experienceOrb.isRemoveScheduled()) {
                        continue;
                    }
                    PickupExperienceEvent pickupExperienceEvent = new PickupExperienceEvent(entity, experienceOrb);
                    EventDispatcher.callCancellable(pickupExperienceEvent, () -> {
                        short experienceCount = pickupExperienceEvent.getExperienceCount();
                        if (entity instanceof Player) {
                            Player player = (Player) entity;
                            player.setExp(player.getExp() + experienceCount);
                        }
                        experienceOrb.remove();
                    });
                }
            }
        }
    }

    /**
     * Gets if the entity is able to pickup items.
     *
     * @return true if the entity is able to pickup items
     */
    public boolean canPickupItem() {
        return canPickupItem;
    }

    /**
     * When set to false, the entity will not be able to pick {@link ItemEntity} on the ground.
     *
     * @param canPickupItem can the entity pickup item
     */
    public void setCanPickupItem(boolean canPickupItem) {
        this.canPickupItem = canPickupItem;
    }

    /**
     * Gets if the entity is able to pickup experience orbs.
     *
     * @return true if the entity is able to pickup experience orbs
     */
    public boolean canPickupExperience() {
        return canPickupExperience;
    }

    /**
     * When set to false, the entity will not be able to pick experience orbs on the ground.
     *
     * @param canPickupExperience can the entity pickup experience orbs
     */
    public void setCanPickupExperience(boolean canPickupExperience) {
        this.canPickupExperience = canPickupExperience;
    }

    public void onBoundingBoxUpdated() {
        this.expandedBoundingBox = entity.getBoundingBox().expand(1D, .5D, 1D);
    }

}
