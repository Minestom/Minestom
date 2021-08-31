package net.minestom.server.entity.features.equipment;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.item.EntityEquipEvent;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class EntityFeatureNpcEquipment extends EntityFeatureEquipment {

    private ItemStack mainHandItem;
    private ItemStack offHandItem;

    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack leggings;
    private ItemStack boots;

    public EntityFeatureNpcEquipment(Entity entity) {
        super(entity);

        this.mainHandItem = ItemStack.AIR;
        this.offHandItem = ItemStack.AIR;

        this.helmet = ItemStack.AIR;
        this.chestplate = ItemStack.AIR;
        this.leggings = ItemStack.AIR;
        this.boots = ItemStack.AIR;
    }

    @NotNull
    @Override
    public ItemStack getItemInMainHand() {
        return mainHandItem;
    }

    @Override
    public void setItemInMainHand(@NotNull ItemStack itemStack) {
        this.mainHandItem = getEquipmentItem(itemStack, EquipmentSlot.MAIN_HAND);
        syncEquipment(EquipmentSlot.MAIN_HAND);
    }

    @NotNull
    @Override
    public ItemStack getItemInOffHand() {
        return offHandItem;
    }

    @Override
    public void setItemInOffHand(@NotNull ItemStack itemStack) {
        this.offHandItem = getEquipmentItem(itemStack, EquipmentSlot.OFF_HAND);
        syncEquipment(EquipmentSlot.OFF_HAND);
    }

    @NotNull
    @Override
    public ItemStack getHelmet() {
        return helmet;
    }

    @Override
    public void setHelmet(@NotNull ItemStack itemStack) {
        this.helmet = getEquipmentItem(itemStack, EquipmentSlot.HELMET);
        syncEquipment(EquipmentSlot.HELMET);
    }

    @NotNull
    @Override
    public ItemStack getChestplate() {
        return chestplate;
    }

    @Override
    public void setChestplate(@NotNull ItemStack itemStack) {
        this.chestplate = getEquipmentItem(itemStack, EquipmentSlot.CHESTPLATE);
        syncEquipment(EquipmentSlot.CHESTPLATE);
    }

    @NotNull
    @Override
    public ItemStack getLeggings() {
        return leggings;
    }

    @Override
    public void setLeggings(@NotNull ItemStack itemStack) {
        this.leggings = getEquipmentItem(itemStack, EquipmentSlot.LEGGINGS);
        syncEquipment(EquipmentSlot.LEGGINGS);
    }

    @NotNull
    @Override
    public ItemStack getBoots() {
        return boots;
    }

    @Override
    public void setBoots(@NotNull ItemStack itemStack) {
        this.boots = getEquipmentItem(itemStack, EquipmentSlot.BOOTS);
        syncEquipment(EquipmentSlot.BOOTS);
    }

    private ItemStack getEquipmentItem(@NotNull ItemStack itemStack, @NotNull EquipmentSlot slot) {
        EntityEquipEvent entityEquipEvent = new EntityEquipEvent(entity, itemStack, slot);
        EventDispatcher.call(entityEquipEvent);
        return entityEquipEvent.getEquippedItem();
    }

}
