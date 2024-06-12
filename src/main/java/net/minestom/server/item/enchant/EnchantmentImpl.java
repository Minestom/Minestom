package net.minestom.server.item.enchant;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.entity.EquipmentSlotGroup;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

record EnchantmentImpl(
        @NotNull NamespaceID namespace,
        @NotNull Component description,
        @NotNull Set<NamespaceID> exclusiveSet,
        @NotNull Set<NamespaceID> supportedItems,
        @NotNull Set<NamespaceID> primaryItems,
        int weight,
        int maxLevel,
        @NotNull Cost minCost,
        @NotNull Cost maxCost,
        int anvilCost,
        @NotNull List<EquipmentSlotGroup> slots,
        @NotNull DataComponentMap effects,
        @Nullable Registry.EnchantmentEntry registry
) implements Enchantment {

    private static final BinaryTagSerializer<List<EquipmentSlotGroup>> SLOTS_NBT_TYPE = EquipmentSlotGroup.NBT_TYPE.list();
    static final BinaryTagSerializer<Enchantment> REGISTRY_NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
            tag -> {
                throw new UnsupportedOperationException("BannerPattern is read-only");
            },
            value -> CompoundBinaryTag.builder()
                    .put("description", BinaryTagSerializer.NBT_COMPONENT.write(value.description()))
                    //todo exclusive_set
                    //todo supported_items
                    //todo primary_items
                    .putInt("weight", value.weight())
                    .putInt("max_level", value.maxLevel())
                    .put("min_cost", Cost.NBT_TYPE.write(value.minCost()))
                    .put("max_cost", Cost.NBT_TYPE.write(value.maxCost()))
                    .putInt("anvil_cost", value.anvilCost())
                    .put("slots", SLOTS_NBT_TYPE.write(value.slots()))
                    .put("effects", EnchantmentEffectComponent.NBT_TYPE.write(value.effects()))
                    .build()
    );

    EnchantmentImpl {
        Check.notNull(namespace, "Namespace cannot be null");
    }

    EnchantmentImpl(@NotNull Registry.EnchantmentEntry registry) {
        //todo sets
        this(registry.namespace(), registry.description(), Set.of(), registry);
    }

}
