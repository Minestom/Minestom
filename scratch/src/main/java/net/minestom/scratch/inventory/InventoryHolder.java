package net.minestom.scratch.inventory;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.client.play.*;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.CloseWindowPacket;
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket;
import net.minestom.server.network.packet.server.play.OpenWindowPacket;
import net.minestom.server.network.packet.server.play.WindowItemsPacket;
import net.minestom.server.utils.SlotUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Manage a player inventory and its potential open container.
 */
public final class InventoryHolder {
    private final int entityId;
    private final Consumer<ServerPacket.Play> selfConsumer;
    private final Consumer<ServerPacket.Play> localBroadcastConsumer;
    private final ItemStack[] inventory = new ItemStack[46];
    private ItemStack cursor = ItemStack.AIR;
    private int heldSlot;

    private Container openContainer;

    public InventoryHolder(int entityId, Consumer<ServerPacket.Play> selfConsumer,
                           Consumer<ServerPacket.Play> localBroadcastConsumer) {
        this.entityId = entityId;
        this.selfConsumer = selfConsumer;
        this.localBroadcastConsumer = localBroadcastConsumer;

        Arrays.fill(inventory, ItemStack.AIR);
    }

    public int heldSlot() {
        return heldSlot;
    }

    public void setItem(int slot, @NotNull ItemStack item) {
        inventory[slot] = item;
    }

    public ItemStack getItem(int slot) {
        return inventory[slot];
    }

    public ItemStack getEquipment(@NotNull EquipmentSlot slot) {
        return inventory[equipmentSlot(slot)];
    }

    public ItemStack getHandItem(PlayerHand hand) {
        final int slot = handSlot(hand);
        return inventory[slot];
    }

    public void setHandItem(PlayerHand hand, ItemStack item) {
        final int slot = handSlot(hand);
        inventory[slot] = item;
    }

    public int handSlot(PlayerHand hand) {
        return switch (hand) {
            case MAIN -> heldSlot;
            case OFF -> SlotUtils.OFFHAND_SLOT;
        };
    }

    public Map<EquipmentSlot, ItemStack> equipments() {
        return Map.of(
                EquipmentSlot.MAIN_HAND, inventory[heldSlot],
                EquipmentSlot.OFF_HAND, inventory[SlotUtils.OFFHAND_SLOT],
                EquipmentSlot.HELMET, inventory[SlotUtils.HELMET_SLOT],
                EquipmentSlot.CHESTPLATE, inventory[SlotUtils.CHESTPLATE_SLOT],
                EquipmentSlot.LEGGINGS, inventory[SlotUtils.LEGGINGS_SLOT],
                EquipmentSlot.BOOTS, inventory[SlotUtils.BOOTS_SLOT]
        );
    }

    public void consumeItem(int slot) {
        ItemStack current = inventory[slot];
        final ItemStack updated = current.withAmount(current.amount() - 1);
        this.inventory[slot] = updated;
    }

    public void consumeItem(PlayerHand hand) {
        final int slot = handSlot(hand);
        consumeItem(slot);
    }

    public WindowItemsPacket itemsPacket() {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < inventory.length; i++) {
            final int internalSlot = SlotUtils.convertPlayerInventorySlot(i, SlotUtils.OFFSET);
            items.add(inventory[internalSlot]);
        }
        return new WindowItemsPacket((byte) 0, 0, items, cursor);
    }

    public EntityEquipmentPacket equipmentPacket() {
        return new EntityEquipmentPacket(entityId, equipments());
    }

    public void consume(ClientHeldItemChangePacket packet) {
        this.heldSlot = packet.slot();
        this.localBroadcastConsumer.accept(equipmentPacket());
    }

    public void consume(ClientClickWindowPacket packet) {
        final List<ClientClickWindowPacket.ChangedSlot> changedSlots = packet.changedSlots();
        Container openContainer = this.openContainer;
        boolean updateContainer = false;
        for (ClientClickWindowPacket.ChangedSlot changedSlot : changedSlots) {
            final int protocolSlot = changedSlot.slot();
            if (openContainer != null) {
                // Has container open
                final int containerSize = openContainer.type.getSize();
                if (protocolSlot < containerSize) {
                    // Click in container
                    openContainer.inventory[protocolSlot] = changedSlot.item();
                    updateContainer = true;
                } else {
                    // Click in player inventory
                    final int internalSlot = SlotUtils.convertSlot(protocolSlot, containerSize);
                    this.inventory[internalSlot] = changedSlot.item();
                }
            } else {
                // No container open
                final int internalSlot = SlotUtils.convertPlayerInventorySlot(protocolSlot, SlotUtils.OFFSET);
                this.inventory[internalSlot] = changedSlot.item();
                if (isEquipmentSlot(internalSlot)) {
                    this.localBroadcastConsumer.accept(equipmentPacket());
                }
            }
        }
        this.cursor = packet.clickedItem();

        if (updateContainer) {
            for (InventoryHolder viewer : openContainer.viewers) {
                if (viewer == this) continue;
                viewer.selfConsumer.accept(openContainer.itemsPacket(viewer.cursor));
            }
        }
    }

    public void consume(ClientUseItemPacket packet) {
        final int slot = switch (packet.hand()) {
            case MAIN -> heldSlot;
            case OFF -> SlotUtils.OFFHAND_SLOT;
        };

        final ItemStack item = inventory[slot];
        final EquipmentSlot equipmentSlot = item.material().registry().equipmentSlot();
        if (equipmentSlot != null && equipmentSlot.isArmor()) {
            final int internalSlot = equipmentSlot(equipmentSlot);
            // Swap the armor piece with the one in the hand
            final ItemStack currentArmor = inventory[internalSlot];
            inventory[internalSlot] = item;
            inventory[slot] = currentArmor;
            this.localBroadcastConsumer.accept(equipmentPacket());
        }
    }

    public void consume(ClientCreativeInventoryActionPacket packet) {
        final int internalSlot = SlotUtils.convertPlayerInventorySlot(packet.slot(), SlotUtils.OFFSET);
        this.inventory[internalSlot] = packet.item();
        if (isEquipmentSlot(internalSlot)) {
            this.localBroadcastConsumer.accept(equipmentPacket());
        }
    }

    public void consume(ClientCloseWindowPacket packet) {
        Container openContainer = this.openContainer;
        if (openContainer != null) {
            openContainer.viewers.remove(this);
            this.openContainer = null;
        }
    }

    private boolean isCraftingSlot(int slot) {
        return slot == SlotUtils.CRAFT_SLOT_1 || slot == SlotUtils.CRAFT_SLOT_2 ||
                slot == SlotUtils.CRAFT_SLOT_3 || slot == SlotUtils.CRAFT_SLOT_4;
    }

    private boolean isHandSlot(int slot) {
        return slot == heldSlot || slot == SlotUtils.OFFHAND_SLOT;
    }

    private boolean isEquipmentSlot(int slot) {
        return switch (slot) {
            case SlotUtils.HELMET_SLOT, SlotUtils.CHESTPLATE_SLOT, SlotUtils.LEGGINGS_SLOT, SlotUtils.BOOTS_SLOT,
                 SlotUtils.OFFHAND_SLOT -> true;
            default -> slot == heldSlot;
        };
    }

    private int equipmentSlot(EquipmentSlot equipmentSlot) {
        return switch (equipmentSlot) {
            case MAIN_HAND -> heldSlot;
            case OFF_HAND -> SlotUtils.OFFHAND_SLOT;
            case HELMET -> SlotUtils.HELMET_SLOT;
            case CHESTPLATE -> SlotUtils.CHESTPLATE_SLOT;
            case LEGGINGS -> SlotUtils.LEGGINGS_SLOT;
            case BOOTS -> SlotUtils.BOOTS_SLOT;
        };
    }

    public boolean openContainer(Container container) {
        final boolean success = container.viewers.add(this);
        if (success) {
            this.openContainer = container;
            selfConsumer.accept(container.openPacket());
            selfConsumer.accept(container.itemsPacket(cursor));
        }
        return success;
    }

    public Container openContainer() {
        return openContainer;
    }

    public void closeContainer() {
        var openContainer = this.openContainer;
        if (openContainer != null) {
            openContainer.viewers.remove(this);
            this.openContainer = null;
            selfConsumer.accept(new CloseWindowPacket(openContainer.id));
        }
    }

    public boolean canAddItem(ItemStack itemStack) {
        int amount = itemStack.amount();
        for (int i = 0; i < 36; i++) {
            ItemStack inventoryItem = inventory[i];
            if (inventoryItem.isAir()) return true;
            if (inventoryItem.isSimilar(itemStack)) {
                final int mergedAmount = inventoryItem.amount() + itemStack.amount();
                final int maxStackSize = inventoryItem.maxStackSize();
                if (mergedAmount <= maxStackSize) return true;
                amount = mergedAmount - maxStackSize;
            }

            if (amount <= 0) return true;
        }
        return false;
    }

    public void addItem(ItemStack itemStack) {
        int amount = itemStack.amount();
        for (int i = 0; i < 36; i++) {
            ItemStack inventoryItem = inventory[i];
            boolean updated = false;
            if (inventoryItem.isAir()) {
                inventory[i] = itemStack;
                updated = true;
                amount = 0;
            } else if (inventoryItem.isSimilar(itemStack)) {
                final int mergedAmount = inventoryItem.amount() + itemStack.amount();
                final int maxStackSize = inventoryItem.maxStackSize();
                if (mergedAmount > maxStackSize) {
                    inventory[i] = inventoryItem.withAmount(maxStackSize);
                    itemStack = itemStack.withAmount(mergedAmount - maxStackSize);
                    amount = itemStack.amount();
                } else {
                    inventory[i] = inventoryItem.withAmount(mergedAmount);
                    amount = 0;
                }
                updated = true;
            }
            if (updated) {
                if (i == heldSlot) this.localBroadcastConsumer.accept(equipmentPacket());
            }
            if (amount <= 0) return;
        }
    }

    public static class Container {
        private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1);
        private final byte id = (byte) ID_GENERATOR.getAndIncrement();
        private final Component title;
        private final InventoryType type;
        private final Set<InventoryHolder> viewers = new HashSet<>();
        private final ItemStack[] inventory;

        public Container(Component title, InventoryType type) {
            this.title = title;
            this.type = type;

            this.inventory = new ItemStack[type.getSize()];
            Arrays.fill(inventory, ItemStack.AIR);
        }

        public OpenWindowPacket openPacket() {
            return new OpenWindowPacket(id, type.ordinal(), title);
        }

        public WindowItemsPacket itemsPacket(ItemStack cursor) {
            return new WindowItemsPacket(id, 0, List.of(inventory), cursor);
        }

        public byte id() {
            return id;
        }

        public InventoryType type() {
            return type;
        }

        public Set<InventoryHolder> viewers() {
            return viewers;
        }

        public ItemStack[] inventory() {
            return inventory;
        }
    }
}
