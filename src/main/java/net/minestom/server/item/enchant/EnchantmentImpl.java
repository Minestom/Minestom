package net.minestom.server.item.enchant;

import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.entity.EquipmentSlotGroup;
import net.minestom.server.item.Material;
import net.minestom.server.registry.RegistryTag;
import org.jspecify.annotations.Nullable;

import java.util.List;

record EnchantmentImpl(
        Component description,
        RegistryTag<Enchantment> exclusiveSet,
        RegistryTag<Material> supportedItems,
        @Nullable RegistryTag<Material> primaryItems,
        int weight,
        int maxLevel,
        Cost minCost,
        Cost maxCost,
        int anvilCost,
        List<EquipmentSlotGroup> slots,
        DataComponentMap effects
) implements Enchantment {

    EnchantmentImpl {
        slots = List.copyOf(slots);
    }

}
