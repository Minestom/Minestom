package net.minestom.server.entity.type.projectile;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Metadata;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @deprecated Use {@link net.minestom.server.entity.metadata.item.ThrownPotionMeta} instead.
 */
@Deprecated
public class EntityPotion extends Entity {

    public EntityPotion(@Nullable Entity shooter, @NotNull Position spawnPosition, @NotNull ItemStack potion) {
        super(EntityType.POTION, spawnPosition);
        setBoundingBox(0.25f, 0.25f, 0.25f);
        setPotion(potion);
    }

    @NotNull
    public ItemStack getPotion() {
        return metadata.getIndex((byte) 7, ItemStack.getAirItem());
    }

    public void setPotion(@NotNull ItemStack potion) {
        this.metadata.setIndex((byte) 7, Metadata.Slot(potion));
    }
}
