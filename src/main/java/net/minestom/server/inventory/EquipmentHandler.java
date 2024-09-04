package net.minestom.server.inventory;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Represents an {@link Entity} which can have {@link ItemStack} in hands and armor slots.
 */
public interface EquipmentHandler {

    /**
     * Gets the equipment in a specific slot.
     *
     * @param slot the equipment to get the item from
     * @return the equipment {@link ItemStack}
     */
    @NotNull ItemStack getEquipment(@NotNull EquipmentSlot slot);

    void setEquipment(@NotNull EquipmentSlot slot, @NotNull ItemStack itemStack);

    /**
     * Gets the {@link ItemStack} in main hand.
     *
     * @return the {@link ItemStack} in main hand
     */
    default @NotNull ItemStack getItemInMainHand() {
        return getEquipment(EquipmentSlot.MAIN_HAND);
    }

    /**
     * Changes the main hand {@link ItemStack}.
     *
     * @param itemStack the main hand {@link ItemStack}
     */
    default void setItemInMainHand(@NotNull ItemStack itemStack) {
        setEquipment(EquipmentSlot.MAIN_HAND, itemStack);
    }

    /**
     * Gets the {@link ItemStack} in off hand.
     *
     * @return the item in off hand
     */
    default @NotNull ItemStack getItemInOffHand() {
        return getEquipment(EquipmentSlot.OFF_HAND);
    }

    /**
     * Changes the off hand {@link ItemStack}.
     *
     * @param itemStack the off hand {@link ItemStack}
     */
    default void setItemInOffHand(@NotNull ItemStack itemStack) {
        setEquipment(EquipmentSlot.OFF_HAND, itemStack);
    }

    /**
     * Gets the {@link ItemStack} in the specific hand.
     *
     * @param hand the Hand to get the {@link ItemStack} from
     * @return the {@link ItemStack} in {@code hand}
     */
    default @NotNull ItemStack getItemInHand(@NotNull PlayerHand hand) {
        return switch (hand) {
            case MAIN -> getItemInMainHand();
            case OFF -> getItemInOffHand();
        };
    }

    /**
     * Changes the {@link ItemStack} in the specific hand.
     *
     * @param hand  the hand to set the item to
     * @param stack the {@link ItemStack} to set
     */
    default void setItemInHand(@NotNull PlayerHand hand, @NotNull ItemStack stack) {
        switch (hand) {
            case MAIN -> setItemInMainHand(stack);
            case OFF -> setItemInOffHand(stack);
        }
    }

    /**
     * Gets the helmet.
     *
     * @return the helmet
     */
    default @NotNull ItemStack getHelmet() {
        return getEquipment(EquipmentSlot.HELMET);
    }

    /**
     * Changes the helmet.
     *
     * @param itemStack the helmet
     */
    default void setHelmet(@NotNull ItemStack itemStack) {
        setEquipment(EquipmentSlot.HELMET, itemStack);
    }

    /**
     * Gets the chestplate.
     *
     * @return the chestplate
     */
    default @NotNull ItemStack getChestplate() {
        return getEquipment(EquipmentSlot.CHESTPLATE);
    }

    /**
     * Changes the chestplate.
     *
     * @param itemStack the chestplate
     */
    default void setChestplate(@NotNull ItemStack itemStack) {
        setEquipment(EquipmentSlot.CHESTPLATE, itemStack);
    }

    /**
     * Gets the leggings.
     *
     * @return the leggings
     */
    default @NotNull ItemStack getLeggings() {
        return getEquipment(EquipmentSlot.LEGGINGS);
    }

    /**
     * Changes the leggings.
     *
     * @param itemStack the leggings
     */
    default void setLeggings(@NotNull ItemStack itemStack) {
        setEquipment(EquipmentSlot.LEGGINGS, itemStack);
    }

    /**
     * Gets the boots.
     *
     * @return the boots
     */
    default @NotNull ItemStack getBoots() {
        return getEquipment(EquipmentSlot.BOOTS);
    }

    /**
     * Changes the boots.
     *
     * @param itemStack the boots
     */
    default void setBoots(@NotNull ItemStack itemStack) {
        setEquipment(EquipmentSlot.BOOTS, itemStack);
    }

    /**
     * Gets the body equipment. Used by horses, wolves, and llama's.
     *
     * @return the body equipment
     */
    default @NotNull ItemStack getBodyEquipment() {
        return getEquipment(EquipmentSlot.BODY);
    }

    /**
     * Changes the body equipment. Used by horses, wolves, and llama's.
     *
     * @param itemStack the body equipment
     */
    default void setBodyEquipment(@NotNull ItemStack itemStack) {
        setEquipment(EquipmentSlot.BODY, itemStack);
    }

    default boolean hasEquipment(@NotNull EquipmentSlot slot) {
        return !getEquipment(slot).isAir();
    }

    /**
     * Sends a specific equipment to viewers.
     *
     * @param slot the slot of the equipment
     */
    default void syncEquipment(@NotNull EquipmentSlot slot) {
        Check.stateCondition(!(this instanceof Entity), "Only accessible for Entity");

        Entity entity = (Entity) this;
        final ItemStack itemStack = getEquipment(slot);
        entity.sendPacketToViewers(new EntityEquipmentPacket(entity.getEntityId(), Map.of(slot, itemStack)));
    }

    /**
     * Gets the packet with all the equipments.
     *
     * @return the packet with the equipments
     * @throws IllegalStateException if 'this' is not an {@link Entity}
     */
    default @NotNull EntityEquipmentPacket getEquipmentsPacket() {
        Check.stateCondition(!(this instanceof Entity), "Only accessible for Entity");
        return new EntityEquipmentPacket(((Entity) this).getEntityId(), Map.of(
                EquipmentSlot.MAIN_HAND, getItemInMainHand(),
                EquipmentSlot.OFF_HAND, getItemInOffHand(),
                EquipmentSlot.BOOTS, getBoots(),
                EquipmentSlot.LEGGINGS, getLeggings(),
                EquipmentSlot.CHESTPLATE, getChestplate(),
                EquipmentSlot.HELMET, getHelmet(),
                EquipmentSlot.BODY, getBodyEquipment()));
        // Some entities do not allow body equipment, in which case the client will ignore this
    }

}
