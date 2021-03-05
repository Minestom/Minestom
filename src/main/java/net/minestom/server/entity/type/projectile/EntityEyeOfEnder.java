package net.minestom.server.entity.type.projectile;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @deprecated Use {@link net.minestom.server.entity.metadata.item.EyeOfEnderMeta} instead.
 */
@Deprecated
public class EntityEyeOfEnder extends Entity {

    public EntityEyeOfEnder(@Nullable Entity shooter, @NotNull Position spawnPosition) {
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

}
