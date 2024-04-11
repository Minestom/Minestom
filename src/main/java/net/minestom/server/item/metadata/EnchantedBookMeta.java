package net.minestom.server.item.metadata;

import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemMetaView;
import net.minestom.server.item.component.CustomData;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.component.ItemComponent;
import net.minestom.server.item.component.ItemComponentPatch;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.HashMap;
import java.util.Map;

@Deprecated
public record EnchantedBookMeta(@NotNull ItemComponentPatch components) implements ItemMetaView<EnchantedBookMeta.Builder> {

    public @NotNull Map<Enchantment, Short> getStoredEnchantmentMap() {
        EnchantmentList value = components.get(ItemComponent.STORED_ENCHANTMENTS, EnchantmentList.EMPTY);
        Map<Enchantment, Short> map = new HashMap<>();
        for (var entry : value.enchantments().entrySet())
            map.put(entry.getKey(), entry.getValue().shortValue());
        return map;
    }

    @Override
    public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        return components.get(ItemComponent.CUSTOM_DATA, CustomData.EMPTY).getTag(tag);
    }

    @Deprecated
    public record Builder(@NotNull ItemComponentPatch.Builder components) implements ItemMetaView.Builder {

        public @NotNull Builder enchantments(@NotNull Map<Enchantment, Short> enchantments) {
            Map<Enchantment, Integer> map = new HashMap<>();
            enchantments.forEach((enchantment, level) -> map.put(enchantment, (int) level));
            // Fetch existing to preserve the showInTooltip value.
            EnchantmentList existing = components.get(ItemComponent.STORED_ENCHANTMENTS, EnchantmentList.EMPTY);
            components.set(ItemComponent.STORED_ENCHANTMENTS, new EnchantmentList(map, existing.showInTooltip()));
            return this;
        }

        public @NotNull Builder enchantment(@NotNull Enchantment enchantment, short level) {
            EnchantmentList value = components.get(ItemComponent.STORED_ENCHANTMENTS, EnchantmentList.EMPTY);
            components.set(ItemComponent.STORED_ENCHANTMENTS, value.with(enchantment, level));
            return this;
        }
    }
}
