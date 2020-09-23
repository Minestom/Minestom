package net.minestom.server.item.metadata;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.NBTUtils;
import net.minestom.server.utils.item.ItemStackUtils;
import net.minestom.server.utils.validate.Check;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;
import org.jglrxavpok.hephaistos.nbt.NBTTypes;

public class CrossbowMeta implements ItemMeta {

    private boolean triple;
    private ItemStack projectile1, projectile2, projectile3;

    private boolean charged;

    /**
     * Set the projectile of this crossbow
     *
     * @param projectile the projectile of the crossbow
     */
    public void setProjectile(ItemStack projectile) {
        Check.argCondition(!ItemStackUtils.isVisible(projectile), "the projectile of your crossbow isn't visible (null or air)");
        this.projectile1 = projectile;
        this.triple = false;
    }

    /**
     * Set the triple projectiles of this crossbow
     *
     * @param projectile1 the projectile 1
     * @param projectile2 the projectile 2
     * @param projectile3 the projectile 3
     */
    public void setProjectiles(ItemStack projectile1, ItemStack projectile2, ItemStack projectile3) {
        Check.argCondition(!ItemStackUtils.isVisible(projectile1), "the projectile1 of your crossbow isn't visible (null or air)");
        Check.argCondition(!ItemStackUtils.isVisible(projectile2), "the projectile2 of your crossbow isn't visible (null or air)");
        Check.argCondition(!ItemStackUtils.isVisible(projectile3), "the projectile3 of your crossbow isn't visible (null or air)");

        this.projectile1 = projectile1;
        this.projectile2 = projectile2;
        this.projectile3 = projectile3;
        this.triple = true;
    }

    /**
     * Get if this crossbow is charged with 3 projectiles
     *
     * @return true if this crossbow is charged with 3 projectiles, false otherwise
     */
    public boolean isTriple() {
        return triple;
    }

    /**
     * Get the first projectile
     *
     * @return the first projectile, null if not present
     */
    public ItemStack getProjectile1() {
        return projectile1;
    }

    /**
     * Get the second projectile
     *
     * @return the second projectile, null if not present
     */
    public ItemStack getProjectile2() {
        return projectile2;
    }

    /**
     * Get the third projectile
     *
     * @return the third projectile, null if not present
     */
    public ItemStack getProjectile3() {
        return projectile3;
    }

    /**
     * Get if the crossbow is currently charged
     *
     * @return true if the crossbow is charged, false otherwise
     */
    public boolean isCharged() {
        return charged;
    }

    /**
     * Make the bow charged or uncharged
     *
     * @param charged true to make the crossbow charged, false otherwise
     */
    public void setCharged(boolean charged) {
        this.charged = charged;
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

        if (projectile1.isSimilar(crossbowMeta.projectile1) &&
                projectile2.isSimilar(crossbowMeta.projectile2) &&
                projectile3.isSimilar(crossbowMeta.projectile3)) {
            return true;
        }

        return !triple && (projectile1.isSimilar(crossbowMeta.projectile1));
    }

    @Override
    public void read(NBTCompound compound) {
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
    public void write(NBTCompound compound) {
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

    private NBTCompound getItemCompound(ItemStack itemStack) {
        NBTCompound compound = new NBTCompound();

        compound.setByte("Count", itemStack.getAmount());
        compound.setString("id", itemStack.getMaterial().getName());
        NBTUtils.saveDataIntoNBT(itemStack, compound);

        return compound;
    }
}
