package net.minestom.server.item.enchant;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.TagStringIOExt;
import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.entity.EquipmentSlotGroup;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.item.Material;
import net.minestom.server.registry.ObjectSet;
import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

record EnchantmentImpl(
        @NotNull NamespaceID namespace,
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
    static final BinaryTagSerializer<Enchantment> REGISTRY_NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
            tag -> {
                return new EnchantmentImpl(
                        NamespaceID.from("blank"),
                        BinaryTagSerializer.NBT_COMPONENT.read(tag.get("description")),
                        ENCHANTMENT_OBJECT_SET_NBT_TYPE.read(tag.get("exclusive_set")),
                        MATERIAL_OBJECT_SET_NBT_TYPE.read(tag.get("supported_items")),
                        MATERIAL_OBJECT_SET_NBT_TYPE.read(tag.get("primary_items")),
                        tag.getInt("weight"),
                        tag.getInt("max_level"),
                        Cost.NBT_TYPE.read(tag.get("min_cost")),
                        Cost.NBT_TYPE.read(tag.get("max_cost")),
                        tag.getInt("anvil_cost"),
                        SLOTS_NBT_TYPE.read(tag.get("slots")),
                        tag.get("effects") instanceof CompoundBinaryTag effects ? EffectComponent.MAP_NBT_TYPE.read(effects) : DataComponentMap.EMPTY,
                        null
                );
            },
            value -> CompoundBinaryTag.builder()
                    .put("description", BinaryTagSerializer.NBT_COMPONENT.write(value.description()))
                    .put("exclusive_set", ENCHANTMENT_OBJECT_SET_NBT_TYPE.write(value.exclusiveSet()))
                    .put("supported_items", MATERIAL_OBJECT_SET_NBT_TYPE.write(value.supportedItems()))
                    .put("primary_items", MATERIAL_OBJECT_SET_NBT_TYPE.write(value.primaryItems()))
                    .putInt("weight", value.weight())
                    .putInt("max_level", value.maxLevel())
                    .put("min_cost", Cost.NBT_TYPE.write(value.minCost()))
                    .put("max_cost", Cost.NBT_TYPE.write(value.maxCost()))
                    .putInt("anvil_cost", value.anvilCost())
                    .put("slots", SLOTS_NBT_TYPE.write(value.slots()))
                    .put("effects", EffectComponent.MAP_NBT_TYPE.write(value.effects()))
                    .build()
    );

    EnchantmentImpl {
        Check.notNull(namespace, "Namespace cannot be null");
    }

    EnchantmentImpl(@NotNull Registry.EnchantmentEntry registry) {
        this(registry.namespace(), fromRawRegistry(registry.raw()), registry);
    }

    EnchantmentImpl(@NotNull NamespaceID namespaceId, @NotNull Enchantment enchantment, @NotNull Registry.EnchantmentEntry registry) {
        this(namespaceId, enchantment.description(), enchantment.exclusiveSet(),
                enchantment.supportedItems(), enchantment.primaryItems(),
                enchantment.weight(), enchantment.maxLevel(), enchantment.minCost(),
                enchantment.maxCost(), enchantment.anvilCost(), enchantment.slots(),
                enchantment.effects(), registry);
    }

    private static @NotNull Enchantment fromRawRegistry(@NotNull String raw) {
        try {
            return REGISTRY_NBT_TYPE.read(TagStringIOExt.readTag(raw));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
