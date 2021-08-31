package net.minestom.server.entity.features.equipment;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class EntityFeaturePlayerEquipment extends EntityFeatureEquipment {

    private final PlayerInventory inventory;

    public EntityFeaturePlayerEquipment(Entity entity) {
        super(entity);
        inventory = ((Player) entity).getInventory();
    }

    @NotNull
    @Override
    public ItemStack getItemInMainHand() {
        return inventory.getItemInMainHand();
    }

    @Override
    public void setItemInMainHand(@NotNull ItemStack itemStack) {
        inventory.setItemInMainHand(itemStack);
    }

    @NotNull
    @Override
    public ItemStack getItemInOffHand() {
        return inventory.getItemInOffHand();
    }

    @Override
    public void setItemInOffHand(@NotNull ItemStack itemStack) {
        inventory.setItemInOffHand(itemStack);
    }

    @NotNull
    @Override
    public ItemStack getHelmet() {
        return inventory.getHelmet();
    }

    @Override
    public void setHelmet(@NotNull ItemStack itemStack) {
        inventory.setHelmet(itemStack);
    }

    @NotNull
    @Override
    public ItemStack getChestplate() {
        return inventory.getChestplate();
    }

    @Override
    public void setChestplate(@NotNull ItemStack itemStack) {
        inventory.setChestplate(itemStack);
    }

    @NotNull
    @Override
    public ItemStack getLeggings() {
        return inventory.getLeggings();
    }

    @Override
    public void setLeggings(@NotNull ItemStack itemStack) {
        inventory.setLeggings(itemStack);
    }

    @NotNull
    @Override
    public ItemStack getBoots() {
        return inventory.getBoots();
    }

    @Override
    public void setBoots(@NotNull ItemStack itemStack) {
        inventory.setBoots(itemStack);
    }
}
