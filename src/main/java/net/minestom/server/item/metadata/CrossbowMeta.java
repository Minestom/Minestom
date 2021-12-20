package net.minestom.server.item.metadata;

import net.minestom.server.item.ItemMeta;
import net.minestom.server.item.ItemMetaBuilder;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.*;

import java.util.ArrayList;
import java.util.List;

public class CrossbowMeta extends ItemMeta implements ItemMetaBuilder.Provider<SpawnEggMeta.Builder> {

    private final boolean triple;
    private final ItemStack projectile1, projectile2, projectile3;
    private final boolean charged;

    protected CrossbowMeta(@NotNull ItemMetaBuilder metaBuilder,
                           boolean triple,
                           ItemStack projectile1, ItemStack projectile2, ItemStack projectile3,
                           boolean charged) {
        super(metaBuilder);
        this.triple = triple;
        this.projectile1 = projectile1;
        this.projectile2 = projectile2;
        this.projectile3 = projectile3;
        this.charged = charged;
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
     * @return the first projectile
     */
    public @NotNull ItemStack getProjectile1() {
        return projectile1;
    }

    /**
     * Gets the second projectile.
     *
     * @return the second projectile
     */
    public @NotNull ItemStack getProjectile2() {
        return projectile2;
    }

    /**
     * Gets the third projectile.
     *
     * @return the third projectile
     */
    public @NotNull ItemStack getProjectile3() {
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

    public static class Builder extends ItemMetaBuilder {

        private boolean triple;
        private ItemStack projectile1 = ItemStack.AIR;
        private ItemStack projectile2 = ItemStack.AIR;
        private ItemStack projectile3 = ItemStack.AIR;
        private boolean charged;

        /**
         * Sets the projectile of this crossbow.
         *
         * @param projectile the projectile of the crossbow, air to remove
         */
        public Builder projectile(@NotNull ItemStack projectile) {
            this.projectile1 = projectile;
            this.triple = false;
            mutableNbt().set("ChargedProjectiles", NBT.List(NBTType.TAG_Compound,
                    projectile.isAir() ? List.of() : List.of(getItemCompound(projectile))));
            return this;
        }

        /**
         * Sets the triple projectiles of this crossbow.
         *
         * @param projectile1 the projectile 1
         * @param projectile2 the projectile 2
         * @param projectile3 the projectile 3
         */
        public Builder projectiles(@NotNull ItemStack projectile1, @NotNull ItemStack projectile2, @NotNull ItemStack projectile3) {
            Check.argCondition(projectile1.isAir(), "the projectile1 of your crossbow isn't visible");
            Check.argCondition(projectile2.isAir(), "the projectile2 of your crossbow isn't visible");
            Check.argCondition(projectile3.isAir(), "the projectile3 of your crossbow isn't visible");
            this.projectile1 = projectile1;
            this.projectile2 = projectile2;
            this.projectile3 = projectile3;
            this.triple = true;
            List<NBTCompound> chargedProjectiles =
                    List.of(getItemCompound(projectile1), getItemCompound(projectile2), getItemCompound(projectile3));
            mutableNbt().set("ChargedProjectiles", NBT.List(NBTType.TAG_Compound, chargedProjectiles));
            return this;
        }

        /**
         * Makes the bow charged or uncharged.
         *
         * @param charged true to make the crossbow charged, false otherwise
         */
        public Builder charged(boolean charged) {
            this.charged = charged;
            mutableNbt().set("Charged", NBT.Boolean(charged));
            return this;
        }

        @Override
        public @NotNull CrossbowMeta build() {
            return new CrossbowMeta(this, triple, projectile1, projectile2, projectile3, charged);
        }

        @Override
        public void read(@NotNull NBTCompound nbtCompound) {
            if (nbtCompound.containsKey("ChargedProjectiles")) {
                final NBTList<NBTCompound> projectilesList = nbtCompound.getList("ChargedProjectiles");
                List<ItemStack> projectiles = new ArrayList<>();
                for (NBTCompound projectileCompound : projectilesList) {
                    final byte count = projectileCompound.getByte("Count");
                    final String id = projectileCompound.getString("id");
                    final Material material = Material.fromNamespaceId(id);

                    final NBTCompound tagsCompound = projectileCompound.getCompound("tag");
                    ItemStack itemStack = ItemStack.fromNBT(material, tagsCompound, count);

                    projectiles.add(itemStack);
                }

                if (projectiles.size() == 1) {
                    this.projectile1 = projectiles.get(0);
                } else if (projectiles.size() == 3) {
                    this.projectile1 = projectiles.get(0);
                    this.projectile2 = projectiles.get(1);
                    this.projectile3 = projectiles.get(2);
                }

            }

            if (nbtCompound.get("Charged") instanceof NBTByte charged) {
                this.charged = charged.asBoolean();
            }
        }

        private @NotNull NBTCompound getItemCompound(@NotNull ItemStack itemStack) {
            NBTCompound compound = itemStack.getMeta().toNBT();
            return compound.modify(n -> {
                n.setByte("Count", (byte) itemStack.getAmount());
                n.setString("id", itemStack.getMaterial().name());
            });
        }
    }
}