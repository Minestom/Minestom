package net.minestom.server.item;

import net.kyori.adventure.text.Component;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.attribute.ItemAttribute;
import net.minestom.server.registry.ProtocolObject;
import net.minestom.server.tag.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.minestom.server.item.ItemSerializers.*;

final class ItemTags {
    static final Tag<Integer> DAMAGE = Tag.Integer("Damage").defaultValue(0);
    static final Tag<Boolean> UNBREAKABLE = Tag.Boolean("Unbreakable").defaultValue(false);
    static final Tag<Integer> HIDE_FLAGS = Tag.Integer("HideFlags").defaultValue(0);
    static final Tag<Integer> CUSTOM_MODEL_DATA = Tag.Integer("CustomModelData").defaultValue(0);
    static final Tag<Component> NAME = Tag.Component("Name").path("display");
    static final Tag<List<Component>> LORE = Tag.Component("Lore").path("display").list().defaultValue(List.of());
    static final Tag<Map<Enchantment, Short>> ENCHANTMENTS = Tag.Structure("Enchantments", ENCHANTMENT_SERIALIZER).list().map(enchantmentEntry -> {
        Map<Enchantment, Short> map = new HashMap<>();
        for (var entry : enchantmentEntry) map.put(entry.enchantment(), entry.level());
        return Map.copyOf(map);
    }, o -> {
        List<EnchantmentEntry> entries = new ArrayList<>();
        for (var entry : o.entrySet()) entries.add(new EnchantmentEntry(entry.getKey(), entry.getValue()));
        return List.copyOf(entries);
    }).defaultValue(Map.of());
    static final Tag<List<ItemAttribute>> ATTRIBUTES = Tag.Structure("AttributeModifiers", ATTRIBUTE_SERIALIZER).list().defaultValue(List.of());
    static final Tag<List<String>> CAN_PLACE_ON = Tag.String("CanPlaceOn").list().defaultValue(List.of());
    static final Tag<List<String>> CAN_DESTROY = Tag.String("CanDestroy").list().defaultValue(List.of());
}
