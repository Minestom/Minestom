package net.minestom.server.inventory;

import net.minestom.server.Viewable;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    @NotNull
    ItemStack getItemInMainHand();

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
    @NotNull
    ItemStack getItemInOffHand();

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
    @NotNull
    default ItemStack getItemInHand(@NotNull Player.Hand hand) {
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
    @NotNull
    ItemStack getHelmet();

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
    @NotNull
    ItemStack getChestplate();

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
    @NotNull
    ItemStack getLeggings();

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
    @NotNull
    ItemStack getBoots();

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
    @NotNull
    default ItemStack getEquipment(@NotNull EntityEquipmentPacket.Slot slot) {
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
     * Sends all the equipments to a {@link PlayerConnection}.
     *
     * @param connection the connection to send the equipments to
     */
    default void syncEquipments(@NotNull PlayerConnection connection) {
        final EntityEquipmentPacket entityEquipmentPacket = getEquipmentsPacket();
        if (entityEquipmentPacket == null)
            return;
        connection.sendPacket(entityEquipmentPacket);
    }

    /**
     * Sends all the equipments to all viewers.
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
     * Sends a specific equipment to viewers.
     *
     * @param slot the slot of the equipment
     */
    default void syncEquipment(@NotNull EntityEquipmentPacket.Slot slot) {
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
     * Gets the packet with all the equipments.
     *
     * @return the packet with the equipments, null if all equipments are air
     * @throws IllegalStateException if 'this' is not an {@link Entity}
     */
    @Nullable
    default EntityEquipmentPacket getEquipmentsPacket() {
        Check.stateCondition(!(this instanceof Entity), "Only accessible for Entity");

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
