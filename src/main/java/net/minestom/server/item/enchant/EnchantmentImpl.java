package net.minestom.server.item.enchant;

import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.entity.EquipmentSlotGroup;
import net.minestom.server.item.Material;
import net.minestom.server.registry.RegistryTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

record EnchantmentImpl(
        @NotNull Component description,
        @NotNull RegistryTag<Enchantment> exclusiveSet,
        @NotNull RegistryTag<Material> supportedItems,
        @Nullable RegistryTag<Material> primaryItems,
        int weight,
        int maxLevel,
        @NotNull Cost minCost,
        @NotNull Cost maxCost,
        int anvilCost,
        @NotNull List<EquipmentSlotGroup> slots,
        @NotNull DataComponentMap effects
) implements Enchantment {

    EnchantmentImpl {
        slots = List.copyOf(slots);
    }

}
