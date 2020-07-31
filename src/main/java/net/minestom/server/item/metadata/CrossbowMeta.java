package net.minestom.server.item.metadata;

import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.item.ItemStackUtils;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

// TODO complete https://minecraft.gamepedia.com/Player.dat_format#Crossbows
public class CrossbowMeta implements ItemMeta {

    private boolean triple;
    private ItemStack projectile1, projectile2, projectile3;

    private boolean charged;

    public void setProjectile(ItemStack projectile) {
        this.projectile1 = projectile1;
        this.triple = false;
    }

    public void setProjectiles(ItemStack projectile1, ItemStack projectile2, ItemStack projectile3) {
        this.projectile1 = projectile1;
        this.projectile2 = projectile2;
        this.projectile3 = projectile3;
        this.triple = true;
    }

    @Override
    public boolean hasNbt() {
        return ItemStackUtils.isVisible(projectile1);
    }

    @Override
    public boolean isSimilar(ItemMeta itemMeta) {
        if (!(itemMeta instanceof CrossbowMeta))
            return false;

        final CrossbowMeta crossbowMeta = (CrossbowMeta) itemMeta;
        final boolean checkCount = triple && crossbowMeta.triple;
        if (!checkCount)
            return false;

        if (triple && (projectile1.isSimilar(crossbowMeta.projectile1)) &&
                (projectile2.isSimilar(crossbowMeta.projectile2)) &&
                (projectile3.isSimilar(crossbowMeta.projectile3))) {
            return true;
        }

        return !triple && (projectile1.isSimilar(crossbowMeta.projectile1));
    }

    @Override
    public void read(NBTCompound compound) {
        // TODO
    }

    @Override
    public void write(NBTCompound compound) {
        // TODO
    }

    @Override
    public ItemMeta clone() {
        CrossbowMeta crossbowMeta = new CrossbowMeta();
        crossbowMeta.triple = triple;
        crossbowMeta.projectile1 = projectile1 == null ? null : projectile1.clone();
        crossbowMeta.projectile2 = projectile2 == null ? null : projectile2.clone();
        crossbowMeta.projectile3 = projectile3 == null ? null : projectile3.clone();

        crossbowMeta.charged = charged;

        return crossbowMeta;
    }
}
