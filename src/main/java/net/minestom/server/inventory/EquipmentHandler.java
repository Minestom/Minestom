package net.minestom.server.inventory;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Player;
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
     * Gets the {@link ItemStack} in main hand.
     *
     * @return the {@link ItemStack} in main hand
     */
    @NotNull ItemStack getItemInMainHand();

    /**
     * Changes the main hand {@link ItemStack}.
     *
     * @param itemStack the main hand {@link ItemStack}
     */
    void setItemInMainHand(@NotNull ItemStack itemStack);

    /**
     * Gets the {@link ItemStack} in off hand.
     *
     * @return the item in off hand
     */
    @NotNull ItemStack getItemInOffHand();

    /**
     * Changes the off hand {@link ItemStack}.
     *
     * @param itemStack the off hand {@link ItemStack}
     */
    void setItemInOffHand(@NotNull ItemStack itemStack);

    /**
     * Gets the {@link ItemStack} in the specific hand.
     *
     * @param hand the Hand to get the {@link ItemStack} from
     * @return the {@link ItemStack} in {@code hand}
     */
    default @NotNull ItemStack getItemInHand(@NotNull Player.Hand hand) {
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
    default void setItemInHand(@NotNull Player.Hand hand, @NotNull ItemStack stack) {
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
    @NotNull ItemStack getHelmet();

    /**
     * Changes the helmet.
     *
     * @param itemStack the helmet
     */
    void setHelmet(@NotNull ItemStack itemStack);

    /**
     * Gets the chestplate.
     *
     * @return the chestplate
     */
    @NotNull ItemStack getChestplate();

    /**
     * Changes the chestplate.
     *
     * @param itemStack the chestplate
     */
    void setChestplate(@NotNull ItemStack itemStack);

    /**
     * Gets the leggings.
     *
     * @return the leggings
     */
    @NotNull ItemStack getLeggings();

    /**
     * Changes the leggings.
     *
     * @param itemStack the leggings
     */
    void setLeggings(@NotNull ItemStack itemStack);

    /**
     * Gets the boots.
     *
     * @return the boots
     */
    @NotNull ItemStack getBoots();

    /**
     * Changes the boots.
     *
     * @param itemStack the boots
     */
    void setBoots(@NotNull ItemStack itemStack);

    /**
     * Gets the equipment in a specific slot.
     *
     * @param slot the equipment to get the item from
     * @return the equipment {@link ItemStack}
     */
    default @NotNull ItemStack getEquipment(@NotNull EquipmentSlot slot) {
        return switch (slot) {
            case MAIN_HAND -> getItemInMainHand();
            case OFF_HAND -> getItemInOffHand();
            case HELMET -> getHelmet();
            case CHESTPLATE -> getChestplate();
            case LEGGINGS -> getLeggings();
            case BOOTS -> getBoots();
        };
    }

    default void setEquipment(@NotNull EquipmentSlot slot, @NotNull ItemStack itemStack) {
        switch (slot) {
            case MAIN_HAND -> setItemInMainHand(itemStack);
            case OFF_HAND -> setItemInOffHand(itemStack);
            case HELMET -> setHelmet(itemStack);
            case CHESTPLATE -> setChestplate(itemStack);
            case LEGGINGS -> setLeggings(itemStack);
            case BOOTS -> setBoots(itemStack);
        }
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
        syncEquipment(slot, getEquipment(slot));
    }

    /**
     * Sends a specific equipment to viewers.
     *
     * @param slot the slot of the equipment
     * @param itemStack the item to be sent for the slot
     */
    default void syncEquipment(@NotNull EquipmentSlot slot, @NotNull ItemStack itemStack) {
        Check.stateCondition(!(this instanceof Entity), "Only accessible for Entity");

        Entity entity = (Entity) this;
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
                EquipmentSlot.HELMET, getHelmet()));
    }

}
