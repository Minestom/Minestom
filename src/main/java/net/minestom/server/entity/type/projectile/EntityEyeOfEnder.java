package net.minestom.server.entity.type.projectile;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.ObjectEntity;
import net.minestom.server.entity.type.Projectile;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.Position;

public class EntityEyeOfEnder extends ObjectEntity implements Projectile {

    public EntityEyeOfEnder(Position spawnPosition) {
        super(EntityType.EYE_OF_ENDER, spawnPosition);
    }


    /**
     * Gets the eye of ender item.
     *
     * @return the item
     */
    public ItemStack getItemStack() {
        return metadata.getIndex((byte) 7, ItemStack.getAirItem());
    }

    /**
     * Changes the eye of ender item.
     * <p>
     * Can be null to make it like {@link Material#ENDER_EYE}.
     *
     * @param itemStack the new item stack
     */
    public void setItemStack(ItemStack itemStack) {
        this.metadata.setIndex((byte) 7, Metadata.Slot(itemStack));
    }

    @Override
    public int getObjectData() {
        return 0;
    }
}
