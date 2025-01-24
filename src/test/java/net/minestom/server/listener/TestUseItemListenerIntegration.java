package net.minestom.server.listener;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.Equippable;
import net.minestom.server.network.packet.client.play.ClientUseItemPacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class TestUseItemListenerIntegration {

    @Test
    void useItemNonSpecial(Env env) {
        // Any random item should not trigger any hand updates
        var instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, 41, 0));

        var useItemCollector = env.trackEvent(PlayerUseItemEvent.class, EventFilter.PLAYER, player);

        var itemStack = ItemStack.of(Material.DIAMOND);
        player.setItemInMainHand(itemStack);
        UseItemListener.useItemListener(new ClientUseItemPacket(PlayerHand.MAIN, 42, 0f, 0f), player);

        useItemCollector.assertSingle(event -> {
            assertEquals(PlayerHand.MAIN, event.getHand());
            assertEquals(itemStack, event.getItemStack());
            assertEquals(0, event.getItemUseTime());
        });
    }

    @Test
    void testEquipArmorToAir(Env env) {
        var instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, 41, 0));

        var boots = ItemStack.of(Material.DIAMOND_BOOTS);
        player.setItemInMainHand(boots);
        UseItemListener.useItemListener(new ClientUseItemPacket(PlayerHand.MAIN, 42, 0f, 0f), player);

        assertEquals(ItemStack.AIR, player.getItemInMainHand());
        assertEquals(boots, player.getEquipment(EquipmentSlot.BOOTS));
    }

    @Test
    void testEquipArmorSwap(Env env) {
        var instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, 41, 0));

        var oldBoots = ItemStack.of(Material.GOLDEN_BOOTS);
        player.setEquipment(EquipmentSlot.BOOTS, oldBoots);

        var boots = ItemStack.of(Material.DIAMOND_BOOTS);
        player.setItemInMainHand(boots);
        UseItemListener.useItemListener(new ClientUseItemPacket(PlayerHand.MAIN, 42, 0f, 0f), player);

        assertEquals(oldBoots, player.getItemInMainHand());
        assertEquals(boots, player.getEquipment(EquipmentSlot.BOOTS));
    }

    @Test
    void testDoNotEquipNonEquippableArmor(Env env) {
        var instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, 41, 0));

        var badBoots = ItemStack.of(Material.GOLDEN_BOOTS).without(ItemComponent.EQUIPPABLE);
        player.setItemInMainHand(badBoots);
        UseItemListener.useItemListener(new ClientUseItemPacket(PlayerHand.MAIN, 42, 0f, 0f), player);

        assertEquals(badBoots, player.getItemInMainHand());
        assertEquals(ItemStack.AIR, player.getEquipment(EquipmentSlot.BOOTS));
    }

    @Test
    void testDoNotSwapNonSwappableArmor(Env env) {
        var instance = env.createFlatInstance();
        var player = env.createPlayer(instance, new Pos(0, 41, 0));

        var oldBoots = ItemStack.of(Material.GOLDEN_BOOTS);
        player.setEquipment(EquipmentSlot.BOOTS, oldBoots);

        var boots = ItemStack.of(Material.DIAMOND_BOOTS).with(ItemComponent.EQUIPPABLE,
                (UnaryOperator<Equippable>) (eq) -> eq.withSwappable(false));
        player.setItemInMainHand(boots);
        UseItemListener.useItemListener(new ClientUseItemPacket(PlayerHand.MAIN, 42, 0f, 0f), player);

        assertEquals(boots, player.getItemInMainHand());
        assertEquals(oldBoots, player.getEquipment(EquipmentSlot.BOOTS));
    }
}
