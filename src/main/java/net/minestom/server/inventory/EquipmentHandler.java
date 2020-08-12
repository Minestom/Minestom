package net.minestom.server.inventory;

import net.minestom.server.Viewable;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket;
import net.minestom.server.network.player.PlayerConnection;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent an entity which can have item in hand and armor slots
 */
public interface EquipmentHandler {

    /**
     * Get the item in main hand
     *
     * @return the item in main hand
     */
    ItemStack getItemInMainHand();

    /**
     * Change the main hand item
     *
     * @param itemStack the main hand item
     */
    void setItemInMainHand(ItemStack itemStack);

    /**
     * Get the item in off hand
     *
     * @return the item in off hand
     */
    ItemStack getItemInOffHand();

    /**
     * Change the off hand item
     *
     * @param itemStack the off hand item
     */
    void setItemInOffHand(ItemStack itemStack);

    /**
     * Get the item in the specific hand
     *
     * @param hand the hand to get the item from
     * @return the item in {@code hand}
     */
    default ItemStack getItemInHand(Player.Hand hand) {
        switch (hand) {
            case MAIN:
                return getItemInMainHand();

            case OFF:
                return getItemInOffHand();

            default:
                return null;
        }
    }

    /**
     * Change the item in the specific hand
     *
     * @param hand  the hand to set the item to
     * @param stack the itemstack to set
     */
    default void setItemInHand(Player.Hand hand, ItemStack stack) {
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
     * Get the helmet
     *
     * @return the helmet
     */
    ItemStack getHelmet();

    /**
     * Change the helmet
     *
     * @param itemStack the helmet
     */
    void setHelmet(ItemStack itemStack);

    /**
     * Get the chestplate
     *
     * @return the chestplate
     */
    ItemStack getChestplate();

    /**
     * Change the chestplate
     *
     * @param itemStack the chestplate
     */
    void setChestplate(ItemStack itemStack);

    /**
     * Get the leggings
     *
     * @return the leggings
     */
    ItemStack getLeggings();

    /**
     * Change the leggings
     *
     * @param itemStack the leggings
     */
    void setLeggings(ItemStack itemStack);

    /**
     * Get the boots
     *
     * @return the boots
     */
    ItemStack getBoots();

    /**
     * Change the boots
     *
     * @param itemStack the boots
     */
    void setBoots(ItemStack itemStack);

    /**
     * Get the equipment in a specific slot
     *
     * @param slot the equipment to get the item from
     * @return the equipment item
     */
    default ItemStack getEquipment(EntityEquipmentPacket.Slot slot) {
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
            default:
                return null;
        }
    }

    /**
     * Send all the equipments to a {@link PlayerConnection}
     *
     * @param connection the connection to send the equipments to
     */
    default void syncEquipments(PlayerConnection connection) {
        final EntityEquipmentPacket entityEquipmentPacket = getEquipmentsPacket();
        if (entityEquipmentPacket == null)
            return;
        connection.sendPacket(entityEquipmentPacket);
    }

    /**
     * Send all the equipments to all viewers
     */
    default void syncEquipments() {
        if (!(this instanceof Viewable))
            throw new IllegalStateException("Only accessible for Entity");

        Viewable viewable = (Viewable) this;

        final EntityEquipmentPacket entityEquipmentPacket = getEquipmentsPacket();
        if (entityEquipmentPacket == null)
            return;

        viewable.sendPacketToViewersAndSelf(entityEquipmentPacket);
    }

    /**
     * Send a specific equipment to viewers
     *
     * @param slot the slot of the equipment
     */
    default void syncEquipment(EntityEquipmentPacket.Slot slot) {
        if (!(this instanceof Entity))
            throw new IllegalStateException("Only accessible for Entity");

        Entity entity = (Entity) this;
        Viewable viewable = (Viewable) this;

        final ItemStack itemStack = getEquipment(slot);

        EntityEquipmentPacket entityEquipmentPacket = new EntityEquipmentPacket();
        entityEquipmentPacket.entityId = entity.getEntityId();
        entityEquipmentPacket.slots = new EntityEquipmentPacket.Slot[]{slot};
        entityEquipmentPacket.itemStacks = new ItemStack[]{itemStack};

        viewable.sendPacketToViewers(entityEquipmentPacket);
    }

    /**
     * Get the packet with all the equipments
     *
     * @return the packet with the equipments
     */
    default EntityEquipmentPacket getEquipmentsPacket() {
        if (!(this instanceof Entity))
            throw new IllegalStateException("Only accessible for Entity");

        Entity entity = (Entity) this;

        EntityEquipmentPacket equipmentPacket = new EntityEquipmentPacket();
        equipmentPacket.entityId = entity.getEntityId();

        List<EntityEquipmentPacket.Slot> slots = new ArrayList<>();
        List<ItemStack> itemStacks = new ArrayList<>();

        for (EntityEquipmentPacket.Slot slot : EntityEquipmentPacket.Slot.values()) {
            final ItemStack itemStack = getEquipment(slot);
            if (!itemStack.isAir()) {
                slots.add(slot);
                itemStacks.add(itemStack);
            }
        }

        if (slots.isEmpty()) {
            return null;
        }

        equipmentPacket.slots = slots.toArray(new EntityEquipmentPacket.Slot[0]);
        equipmentPacket.itemStacks = itemStacks.toArray(new ItemStack[0]);
        return equipmentPacket;
    }

}
