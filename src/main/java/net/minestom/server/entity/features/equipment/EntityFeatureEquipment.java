package net.minestom.server.entity.features.equipment;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.features.EntityFeatureBase;
import net.minestom.server.inventory.EquipmentHandler;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class EntityFeatureEquipment extends EntityFeatureBase implements EquipmentHandler {

    protected EntityFeatureEquipment(Entity entity) {
        super(entity);
    }

    @Override
    public void onAddViewer(Player player) {
        player.getPlayerConnection().sendPacket(getEquipmentsPacket());
    }

    /**
     * Sends a specific equipment to viewers.
     *
     * @param slot the slot of the equipment
     */
    public void syncEquipment(@NotNull EquipmentSlot slot) {
        final ItemStack itemStack = getEquipment(slot);

        EntityEquipmentPacket entityEquipmentPacket = new EntityEquipmentPacket();
        entityEquipmentPacket.entityId = entity.getEntityId();
        entityEquipmentPacket.slots = new EquipmentSlot[]{slot};
        entityEquipmentPacket.itemStacks = new ItemStack[]{itemStack};

        entity.sendPacketToViewers(entityEquipmentPacket);
    }

    /**
     * Gets the packet with all the equipments.
     *
     * @return the packet with the equipments
     * @throws IllegalStateException if 'this' is not an {@link Entity}
     */
    public @NotNull EntityEquipmentPacket getEquipmentsPacket() {
        final EquipmentSlot[] slots = EquipmentSlot.values();

        List<ItemStack> itemStacks = new ArrayList<>(slots.length);

        // Fill items
        for (EquipmentSlot slot : slots) {
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
