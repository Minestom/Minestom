package net.minestom.server.item.enchant;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.TagStringIOExt;
import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.entity.EquipmentSlotGroup;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.item.Material;
import net.minestom.server.registry.ObjectSet;
import net.minestom.server.registry.Registries;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
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

    private static final BinaryTagSerializer<ObjectSet<Enchantment>> ENCHANTMENT_OBJECT_SET_NBT_TYPE = ObjectSet.nbtType(Tag.BasicType.ENCHANTMENTS);
    private static final BinaryTagSerializer<ObjectSet<Material>> MATERIAL_OBJECT_SET_NBT_TYPE = ObjectSet.nbtType(Tag.BasicType.ITEMS);
    private static final BinaryTagSerializer<List<EquipmentSlotGroup>> SLOTS_NBT_TYPE = EquipmentSlotGroup.NBT_TYPE.list();
    static final BinaryTagSerializer<Enchantment> REGISTRY_NBT_TYPE = new BinaryTagSerializer<>() {
        @Override
        public @NotNull BinaryTag write(@NotNull Context context, @NotNull Enchantment value) {
            return CompoundBinaryTag.builder()
                    .put("description", BinaryTagSerializer.NBT_COMPONENT.write(context, value.description()))
                    .put("exclusive_set", ENCHANTMENT_OBJECT_SET_NBT_TYPE.write(context, value.exclusiveSet()))
                    .put("supported_items", MATERIAL_OBJECT_SET_NBT_TYPE.write(context, value.supportedItems()))
                    .put("primary_items", MATERIAL_OBJECT_SET_NBT_TYPE.write(context, value.primaryItems()))
                    .putInt("weight", value.weight())
                    .putInt("max_level", value.maxLevel())
                    .put("min_cost", Cost.NBT_TYPE.write(context, value.minCost()))
                    .put("max_cost", Cost.NBT_TYPE.write(context, value.maxCost()))
                    .putInt("anvil_cost", value.anvilCost())
                    .put("slots", SLOTS_NBT_TYPE.write(context, value.slots()))
                    .put("effects", EffectComponent.MAP_NBT_TYPE.write(context, value.effects()))
                    .build();
        }

        @Override
        public @NotNull Enchantment read(@NotNull Context context, @NotNull BinaryTag raw) {
            if (!(raw instanceof CompoundBinaryTag tag)) throw new IllegalArgumentException("Expected a compound tag");
            return new EnchantmentImpl(
                    BinaryTagSerializer.NBT_COMPONENT.read(context, tag.get("description")),
                    ENCHANTMENT_OBJECT_SET_NBT_TYPE.read(context, tag.get("exclusive_set")),
                    MATERIAL_OBJECT_SET_NBT_TYPE.read(context, tag.get("supported_items")),
                    MATERIAL_OBJECT_SET_NBT_TYPE.read(context, tag.get("primary_items")),
                    tag.getInt("weight"),
                    tag.getInt("max_level"),
                    Cost.NBT_TYPE.read(context, tag.get("min_cost")),
                    Cost.NBT_TYPE.read(context, tag.get("max_cost")),
                    tag.getInt("anvil_cost"),
                    SLOTS_NBT_TYPE.read(context, tag.get("slots")),
                    tag.get("effects") instanceof CompoundBinaryTag effects ? EffectComponent.MAP_NBT_TYPE.read(context, effects) : DataComponentMap.EMPTY,
                    null
                );
        }
    };

    EnchantmentImpl {
        slots = List.copyOf(slots);
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
            BinaryTagSerializer.Context context = new BinaryTagSerializer.ContextWithRegistries(registries, false);
            return REGISTRY_NBT_TYPE.read(context, TagStringIOExt.readTag(raw));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
