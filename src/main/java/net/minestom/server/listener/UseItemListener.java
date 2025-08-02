package net.minestom.server.listener;

import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.item.PlayerBeginItemUseEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemAnimation;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.Consumable;
import net.minestom.server.item.component.Equippable;
import net.minestom.server.item.component.InstrumentComponent;
import net.minestom.server.item.instrument.Instrument;
import net.minestom.server.network.packet.client.play.ClientUseItemPacket;
import net.minestom.server.network.packet.server.play.AcknowledgeBlockChangePacket;

public class UseItemListener {

    public static void useItemListener(ClientUseItemPacket packet, Player player) {
        final PlayerHand hand = packet.hand();
        final ItemStack itemStack = player.getItemInHand(hand);
        final Material material = itemStack.material();
        final Consumable consumable = itemStack.get(DataComponents.CONSUMABLE);

        // The following item animations and use item times come from vanilla.
        // These items do not yet use components, but hopefully they will in the future
        // and this behavior can be removed.
        long useItemTime = 0;
        ItemAnimation useAnimation = ItemAnimation.NONE;
        if (material == Material.BOW) {
            useItemTime = 72000;
            useAnimation = ItemAnimation.BOW;
        } else if (material == Material.CROSSBOW) {
            // The crossbow has a min charge time dependent on quick charge, but to the
            // client they can hold it forever
            useItemTime = 7200;
            useAnimation = ItemAnimation.CROSSBOW;
        } else if (material == Material.SHIELD) {
            useItemTime = 72000;
            useAnimation = ItemAnimation.BLOCK;
        } else if (material == Material.TRIDENT) {
            useItemTime = 72000;
            useAnimation = ItemAnimation.SPEAR;
        } else if (material == Material.SPYGLASS) {
            useItemTime = 1200;
            useAnimation = ItemAnimation.SPYGLASS;
        } else if (material == Material.GOAT_HORN) {
            useItemTime = getInstrumentTime(itemStack);
            useAnimation = ItemAnimation.TOOT_HORN;
        } else if (material == Material.BRUSH) {
            useItemTime = 200;
            useAnimation = ItemAnimation.BRUSH;
        } else if (material.name().contains("bundle")) {
            // Why is a bundle usable???
            useItemTime = 200;
            useAnimation = ItemAnimation.BUNDLE;
        } else if (consumable != null) {
            useItemTime = consumable.consumeTicks();
            useAnimation = consumable.animation();
        }

        boolean usingMainHand = player.getItemUseHand() == PlayerHand.MAIN && hand == PlayerHand.OFF;
        PlayerUseItemEvent useItemEvent = new PlayerUseItemEvent(player, hand, itemStack,
                usingMainHand ? 0 : useItemTime);
        EventDispatcher.call(useItemEvent);

        player.sendPacket(new AcknowledgeBlockChangePacket(packet.sequence()));
        final PlayerInventory playerInventory = player.getInventory();
        if (useItemEvent.isCancelled()) {
            playerInventory.update();
            return;
        }

        useItemTime = useItemEvent.getItemUseTime();
        if (useItemTime != 0) {
            final PlayerBeginItemUseEvent beginUseEvent = new PlayerBeginItemUseEvent(player, hand, itemStack, useAnimation, useItemTime);
            EventDispatcher.callCancellable(beginUseEvent, () -> {
                if (beginUseEvent.getItemUseDuration() <= 0) return;

                player.refreshItemUse(hand, beginUseEvent.getItemUseDuration());
                player.refreshActiveHand(true, hand == PlayerHand.OFF, false);
            });

            return; // Do not also swap after use
        }

        // If the item was not usable, we can try to do an equipment swap with it.
        final Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
        if (equippable != null && equippable.swappable() && equippable.slot().armorSlot() > 0) {
            final ItemStack currentlyEquipped = player.getEquipment(equippable.slot());
            player.setEquipment(equippable.slot(), itemStack);
            player.setItemInHand(hand, currentlyEquipped);
        }
    }

    private static int getInstrumentTime(ItemStack itemStack) {
        final InstrumentComponent holder = itemStack.get(DataComponents.INSTRUMENT);
        if (holder == null) return 0;

        final Instrument instrument = holder.resolve(MinecraftServer.getInstrumentRegistry());
        if (instrument == null) return 0;

        return instrument.useDurationTicks();
    }
}
