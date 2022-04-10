package net.minestom.server.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minestom.server.tag.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static net.minestom.server.item.ItemSerializers.ENCHANTMENT_SERIALIZER;
import static net.minestom.server.item.ItemSerializers.EnchantmentEntry;

final class ItemTags {
    static final Tag<Integer> DAMAGE = Tag.Integer("Damage").defaultValue(0);
    static final Tag<Byte> UNBREAKABLE = Tag.Byte("Unbreakable").defaultValue((byte) 0);
    static final Tag<Integer> HIDE_FLAGS = Tag.Integer("HideFlags").defaultValue(0);
    static final Tag<Integer> CUSTOM_MODEL_DATA = Tag.Integer("CustomModelData").defaultValue(0);

    // Display
    static final Tag<Component> NAME = Tag.String("Name").path("display").map(stringToComponent(), componentToString());
    static final Tag<List<Component>> LORE = Tag.String("Lore").path("display").map(stringToComponent(), componentToString()).list().defaultValue(List.of());

    // Enchantments
    static final Tag<Map<Enchantment, Short>> ENCHANTMENTS = Tag.Structure("Enchantments", ENCHANTMENT_SERIALIZER).list().map(enchantmentEntry -> {
        Map<Enchantment, Short> map = new HashMap<>();
        for (var entry : enchantmentEntry) map.put(entry.enchantment(), entry.level());
        return Map.copyOf(map);
    }, o -> {
        List<EnchantmentEntry> entries = new ArrayList<>();
        for (var entry : o.entrySet()) entries.add(new EnchantmentEntry(entry.getKey(), entry.getValue()));
        return List.copyOf(entries);
    }).defaultValue(Map.of());

    // Attributes
    //static final Tag<List<ItemAttribute>> ATTRIBUTES = Tag.String("AttributeModifiers").map(toAttribute(), fromAttribute()).list();

    // Functions

    static Function<String, Component> stringToComponent() {
        return s -> {
            if (s == null) return null;
            return GsonComponentSerializer.gson().deserialize(s);
        };
    }

    static Function<Component, String> componentToString() {
        return GsonComponentSerializer.gson()::serialize;
    }
}
