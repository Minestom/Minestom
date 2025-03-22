package net.minestom.server.item.enchant;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.TagStringIOExt;
import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.entity.EquipmentSlotGroup;
import net.minestom.server.item.Material;
import net.minestom.server.registry.ObjectSet;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.Registry;
import net.minestom.server.registry.RegistryTranscoder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

record EnchantmentImpl(
        @NotNull Component description,
        @NotNull ObjectSet<Enchantment> exclusiveSet,
        @NotNull ObjectSet<Material> supportedItems,
        @NotNull ObjectSet<Material> primaryItems,
        int weight,
        int maxLevel,
        @NotNull Cost minCost,
        @NotNull Cost maxCost,
        int anvilCost,
        @NotNull List<EquipmentSlotGroup> slots,
        @NotNull DataComponentMap effects,
        @Nullable Registry.EnchantmentEntry registry
) implements Enchantment {

    EnchantmentImpl {
        slots = List.copyOf(slots);
    }

    EnchantmentImpl(@NotNull Component description, @NotNull ObjectSet<Enchantment> exclusiveSet, @NotNull ObjectSet<Material> supportedItems, @NotNull ObjectSet<Material> primaryItems, int weight, int maxLevel, @NotNull Cost minCost, @NotNull Cost maxCost, int anvilCost, @NotNull List<EquipmentSlotGroup> slots, @NotNull DataComponentMap effects) {
        this(description, exclusiveSet, supportedItems, primaryItems, weight, maxLevel, minCost, maxCost, anvilCost, slots, effects, null);
    }

    EnchantmentImpl(@NotNull Registries registries, @NotNull Registry.EnchantmentEntry registry) {
        this(fromRawRegistry(registries, registry.raw()), registry);
    }

    EnchantmentImpl(@NotNull Enchantment enchantment, @NotNull Registry.EnchantmentEntry registry) {
        this(enchantment.description(), enchantment.exclusiveSet(),
                enchantment.supportedItems(), enchantment.primaryItems(),
                enchantment.weight(), enchantment.maxLevel(), enchantment.minCost(),
                enchantment.maxCost(), enchantment.anvilCost(), enchantment.slots(),
                enchantment.effects(), registry);
    }

    private static @NotNull Enchantment fromRawRegistry(@NotNull Registries registries, @NotNull String raw) {
        try {
            final Transcoder<BinaryTag> coder = new RegistryTranscoder<>(Transcoder.NBT, registries);
            return REGISTRY_CODEC.decode(coder, TagStringIOExt.readTag(raw)).orElseThrow("Invalid enchantment");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
