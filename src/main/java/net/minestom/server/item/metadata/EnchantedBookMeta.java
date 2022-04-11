package net.minestom.server.item.metadata;

import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemMetaView;
import net.minestom.server.item.ItemSerializers;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import net.minestom.server.tag.TagReadable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.minestom.server.item.ItemSerializers.ENCHANTMENT_SERIALIZER;

public record EnchantedBookMeta(TagReadable readable) implements ItemMetaView<EnchantedBookMeta.Builder> {
    static final Tag<Map<Enchantment, Short>> ENCHANTMENTS = Tag.Structure("StoredEnchantments", ENCHANTMENT_SERIALIZER).list().map(enchantmentEntry -> {
        Map<Enchantment, Short> map = new HashMap<>();
        for (var entry : enchantmentEntry) map.put(entry.enchantment(), entry.level());
        return Map.copyOf(map);
    }, o -> {
        List<ItemSerializers.EnchantmentEntry> entries = new ArrayList<>();
        for (var entry : o.entrySet())
            entries.add(new ItemSerializers.EnchantmentEntry(entry.getKey(), entry.getValue()));
        return List.copyOf(entries);
    }).defaultValue(Map.of());

    public @NotNull Map<Enchantment, Short> getStoredEnchantmentMap() {
        return getTag(ENCHANTMENTS);
    }

    @Override
    public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        return readable.getTag(tag);
    }

    public record Builder(TagHandler tagHandler) implements ItemMetaView.Builder {
        public @NotNull Builder enchantments(@NotNull Map<Enchantment, Short> enchantments) {
            setTag(ENCHANTMENTS, Map.copyOf(enchantments));
            return this;
        }

        public @NotNull Builder enchantment(@NotNull Enchantment enchantment, short level) {
            var enchantments = new HashMap<>(getTag(ENCHANTMENTS));
            enchantments.put(enchantment, level);
            return enchantments(enchantments);
        }
    }
}
