package net.minestom.server.inventory.type;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.RegistryKey;
import net.minestom.testing.RegistriesTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RegistriesTest
class EnchantmentTableInventoryRegistriesTest {

    @Test
    void displayedEnchantmentRoundTrips(Registries registries) {
        final var inventory = new EnchantmentTableInventory(Component.text("Enchant"), registries.enchantment());
        assertNull(inventory.getEnchantmentShown(EnchantmentTableInventory.EnchantmentSlot.MIDDLE));

        inventory.setEnchantmentShown(EnchantmentTableInventory.EnchantmentSlot.MIDDLE, Enchantment.SHARPNESS);
        assertEquals(Enchantment.SHARPNESS,
                inventory.getEnchantmentShown(EnchantmentTableInventory.EnchantmentSlot.MIDDLE));

        inventory.setEnchantmentShown(EnchantmentTableInventory.EnchantmentSlot.MIDDLE, null);
        assertNull(inventory.getEnchantmentShown(EnchantmentTableInventory.EnchantmentSlot.MIDDLE));
    }

    @Test
    void unknownEnchantmentIsRejected(Registries registries) {
        final var inventory = new EnchantmentTableInventory(Component.text("Enchant"), registries.enchantment());
        final RegistryKey<Enchantment> unknown = RegistryKey.unsafeOf("minestom:unknown_enchantment");

        assertThrows(IllegalArgumentException.class, () ->
                inventory.setEnchantmentShown(EnchantmentTableInventory.EnchantmentSlot.MIDDLE, unknown));
    }
}
