package net.minestom.server.inventory;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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
        switch (hand) {
            case MAIN:
                return getItemInMainHand();
            case OFF:
                return getItemInOffHand();
        }
        throw new IllegalStateException("Something weird happened");
    }

    /**
     * Changes the {@link ItemStack} in the specific hand.
     *
     * @param hand  the hand to set the item to
     * @param stack the {@link ItemStack} to set
     */
    default void setItemInHand(@NotNull Player.Hand hand, @NotNull ItemStack stack) {
        switch (hand) {
            case MAIN:
                setItemInMainHand(stack);
                break;

            case OFF:
                setItemInOffHand(stack);
                break;
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
    default @NotNull ItemStack getEquipment(@NotNull EntityEquipmentPacket.Slot slot) {
        switch (slot) {
            case MAIN_HAND:
                return getItemInMainHand();
            case OFF_HAND:
                return getItemInOffHand();
            case HELMET:
                return getHelmet();
            case CHESTPLATE:
                return getChestplate();
            case LEGGINGS:
                return getLeggings();
            case BOOTS:
                return getBoots();
        }
        throw new IllegalStateException("Something weird happened");
    }

    default void setEquipment(@NotNull EntityEquipmentPacket.Slot slot, @NotNull ItemStack itemStack) {
        switch (slot) {
            case MAIN_HAND:
                setItemInMainHand(itemStack);
                break;
            case OFF_HAND:
                setItemInOffHand(itemStack);
                break;
            case HELMET:
                setHelmet(itemStack);
                break;
            case CHESTPLATE:
                setChestplate(itemStack);
                break;
            case LEGGINGS:
                setLeggings(itemStack);
                break;
            case BOOTS:
                setBoots(itemStack);
                break;
            default:
                throw new IllegalStateException("Something weird happened");
        }
    }

    /**
     * Sends a specific equipment to viewers.
     *
     * @param slot the slot of the equipment
     */
    default void syncEquipment(@NotNull EntityEquipmentPacket.Slot slot) {
        Check.stateCondition(!(this instanceof Entity), "Only accessible for Entity");

        Entity entity = (Entity) this;

        final ItemStack itemStack = getEquipment(slot);

        EntityEquipmentPacket entityEquipmentPacket = new EntityEquipmentPacket();
        entityEquipmentPacket.entityId = entity.getEntityId();
        entityEquipmentPacket.slots = new EntityEquipmentPacket.Slot[]{slot};
        entityEquipmentPacket.itemStacks = new ItemStack[]{itemStack};

        entity.sendPacketToViewers(entityEquipmentPacket);
    }

    /**
     * Gets the packet with all the equipments.
     *
     * @return the packet with the equipments
     * @throws IllegalStateException if 'this' is not an {@link Entity}
     */
    default @NotNull EntityEquipmentPacket getEquipmentsPacket() {
        Check.stateCondition(!(this instanceof Entity), "Only accessible for Entity");

        final Entity entity = (Entity) this;

        final EntityEquipmentPacket.Slot[] slots = EntityEquipmentPacket.Slot.values();

        List<ItemStack> itemStacks = new ArrayList<>(slots.length);

        // Fill items
        for (EntityEquipmentPacket.Slot slot : slots) {
            final ItemStack equipment = getEquipment(slot);
            itemStacks.add(equipment);
        }

        // Create equipment packet
        EntityEquipmentPacket equipmentPacket = new EntityEquipmentPacket();
        equipmentPacket.entityId = entity.getEntityId();
        equipmentPacket.slots = slots;
        equipmentPacket.itemStacks = itemStacks.toArray(new ItemStack[0]);
        return equipmentPacket;
    }

}
