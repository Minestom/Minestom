package net.minestom.server.item.metadata;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.NBTUtils;
import net.minestom.server.utils.clone.CloneUtils;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;
import org.jglrxavpok.hephaistos.nbt.NBTTypes;

public class CrossbowMeta extends ItemMeta {

    private boolean triple;
    private ItemStack projectile1, projectile2, projectile3;

    private boolean charged;

    /**
     * Sets the projectile of this crossbow.
     *
     * @param projectile the projectile of the crossbow
     */
    public void setProjectile(@NotNull ItemStack projectile) {
        Check.argCondition(projectile.isAir(), "the projectile of your crossbow isn't visible");
        this.projectile1 = projectile;
        this.triple = false;
    }

    /**
     * Sets the triple projectiles of this crossbow.
     *
     * @param projectile1 the projectile 1
     * @param projectile2 the projectile 2
     * @param projectile3 the projectile 3
     */
    public void setProjectiles(@NotNull ItemStack projectile1, @NotNull ItemStack projectile2, @NotNull ItemStack projectile3) {
        Check.argCondition(projectile1.isAir(), "the projectile1 of your crossbow isn't visible");
        Check.argCondition(projectile2.isAir(), "the projectile2 of your crossbow isn't visible");
        Check.argCondition(projectile3.isAir(), "the projectile3 of your crossbow isn't visible");

        this.projectile1 = projectile1;
        this.projectile2 = projectile2;
        this.projectile3 = projectile3;
        this.triple = true;
    }

    /**
     * Gets if this crossbow is charged with 3 projectiles.
     *
     * @return true if this crossbow is charged with 3 projectiles, false otherwise
     */
    public boolean isTriple() {
        return triple;
    }

    /**
     * Gets the first projectile.
     *
     * @return the first projectile, null if not present
     */
    public ItemStack getProjectile1() {
        return projectile1;
    }

    /**
     * Gets the second projectile.
     *
     * @return the second projectile, null if not present
     */
    public ItemStack getProjectile2() {
        return projectile2;
    }

    /**
     * Gets the third projectile.
     *
     * @return the third projectile, null if not present
     */
    public ItemStack getProjectile3() {
        return projectile3;
    }

    /**
     * Gets if the crossbow is currently charged.
     *
     * @return true if the crossbow is charged, false otherwise
     */
    public boolean isCharged() {
        return charged;
    }

    /**
     * Makes the bow charged or uncharged.
     *
     * @param charged true to make the crossbow charged, false otherwise
     */
    public void setCharged(boolean charged) {
        this.charged = charged;
    }

    @Override
    public boolean hasNbt() {
        return projectile1 != null && !projectile1.isAir();
    }

    @Override
    public boolean isSimilar(@NotNull ItemMeta itemMeta) {
        if (!(itemMeta instanceof CrossbowMeta))
            return false;

        final CrossbowMeta crossbowMeta = (CrossbowMeta) itemMeta;
        final boolean checkCount = triple && crossbowMeta.triple;
        if (!checkCount)
            return false;

        if (projectile1.isSimilar(crossbowMeta.projectile1) &&
                projectile2.isSimilar(crossbowMeta.projectile2) &&
                projectile3.isSimilar(crossbowMeta.projectile3)) {
            return true;
        }

        return !triple && (projectile1.isSimilar(crossbowMeta.projectile1));
    }

    @Override
    public void read(@NotNull NBTCompound compound) {
        if (compound.containsKey("ChargedProjectiles")) {
            final NBTList<NBTCompound> projectilesList = compound.getList("ChargedProjectiles");
            int index = 0;
            for (NBTCompound projectileCompound : projectilesList) {
                final byte count = projectileCompound.getByte("Count");
                final String id = projectileCompound.getString("id");
                final Material material = Registries.getMaterial(id);

                final NBTCompound tagsCompound = projectileCompound.getCompound("tag");

                ItemStack itemStack = new ItemStack(material, count);
                NBTUtils.loadDataIntoItem(itemStack, tagsCompound);

                index++;

                if (index == 1) {
                    projectile1 = itemStack;
                } else if (index == 2) {
                    projectile2 = itemStack;
                } else if (index == 3) {
                    projectile3 = itemStack;
                }

            }
        }

        if (compound.containsKey("Charged")) {
            this.charged = compound.getByte("Charged") == 1;
        }
    }

    @Override
    public void write(@NotNull NBTCompound compound) {
        if (projectile1 != null || projectile2 != null || projectile3 != null) {
            NBTList<NBTCompound> chargedProjectiles = new NBTList<>(NBTTypes.TAG_Compound);
            if (projectile1 != null) {
                chargedProjectiles.add(getItemCompound(projectile1));
            }
            if (projectile2 != null) {
                chargedProjectiles.add(getItemCompound(projectile2));
            }
            if (projectile3 != null) {
                chargedProjectiles.add(getItemCompound(projectile3));
            }
            compound.set("ChargedProjectiles", chargedProjectiles);
        }

        if (charged) {
            compound.setByte("Charged", (byte) (charged ? 1 : 0));
        }
    }

    @NotNull
    @Override
    public ItemMeta clone() {
        CrossbowMeta crossbowMeta = (CrossbowMeta) super.clone();
        crossbowMeta.triple = triple;
        crossbowMeta.projectile1 = CloneUtils.optionalClone(projectile1);
        crossbowMeta.projectile2 = CloneUtils.optionalClone(projectile2);
        crossbowMeta.projectile3 = CloneUtils.optionalClone(projectile3);

        crossbowMeta.charged = charged;

        return crossbowMeta;
    }

    @NotNull
    private NBTCompound getItemCompound(@NotNull ItemStack itemStack) {
        NBTCompound compound = new NBTCompound();

        compound.setByte("Count", itemStack.getAmount());
        compound.setString("id", itemStack.getMaterial().getName());
        NBTUtils.saveDataIntoNBT(itemStack, compound);

        return compound;
    }
}
