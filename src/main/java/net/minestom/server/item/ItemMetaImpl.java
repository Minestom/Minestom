package net.minestom.server.item;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.TagStringIO;
import net.kyori.adventure.text.Component;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.attribute.ItemAttribute;
import net.minestom.server.item.component.*;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

@Deprecated
record ItemMetaImpl(ItemComponentPatch components) implements ItemMeta {

    @Override
    public <T> @UnknownNullability T getTag(@NotNull Tag<T> tag) {
        return components.get(ItemComponent.CUSTOM_DATA, CustomData.EMPTY).getTag(tag);
    }

    @Override
    public @NotNull ItemMeta with(@NotNull Consumer<ItemMeta.@NotNull Builder> builderConsumer) {
        Builder builder = new Builder(components.builder());
        builderConsumer.accept(builder);
        return builder.build();
    }

    @Override
    public @NotNull CompoundBinaryTag toNBT() {
        return components.asCompound();
    }

    @Override
    public @NotNull String toSNBT() {
        try {
            return TagStringIO.get().asString(toNBT());
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert to SNBT", e);
        }
    }

    @Override
    public int getDamage() {
        return components.get(ItemComponent.DAMAGE, 0);
    }

    @Override
    public boolean isUnbreakable() {
        return components.has(ItemComponent.UNBREAKABLE);
    }

    @Override
    public int getHideFlag() {
        return 0;
    }

    @Override
    public @Nullable Component getDisplayName() {
        return components.get(ItemComponent.CUSTOM_NAME);
    }

    @Override
    public @NotNull List<@NotNull Component> getLore() {
        return components.get(ItemComponent.LORE, List.of());
    }

    @Override
    public @NotNull Map<Enchantment, Short> getEnchantmentMap() {
        EnchantmentList enchantments = components.get(ItemComponent.ENCHANTMENTS);
        if (enchantments == null) return Map.of();
        Map<Enchantment, Short> map = new HashMap<>(enchantments.enchantments().size());
        for (Map.Entry<Enchantment, Integer> entry : enchantments.enchantments().entrySet()) {
            map.put(entry.getKey(), entry.getValue().shortValue());
        }
        return map;
    }

    @Override
    public @NotNull List<@NotNull ItemAttribute> getAttributes() {
        //todo
    }

    @Override
    public int getCustomModelData() {
        return components.get(ItemComponent.CUSTOM_MODEL_DATA, 0);
    }

    @Override
    public @NotNull Set<@NotNull String> getCanDestroy() {
        //todo
    }

    @Override
    public boolean canDestroy(@NotNull Block block) {
        //todo
    }

    @Override
    public @NotNull Set<@NotNull String> getCanPlaceOn() {
        //todo
    }

    @Override
    public boolean canPlaceOn(@NotNull Block block) {
        //todo
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemMetaImpl itemMeta)) return false;
        return components.equals(itemMeta.components);
    }

    @Override
    public int hashCode() {
        return Objects.hash(components);
    }

    @Override
    public String toString() {
        return toSNBT();
    }

    static final class Builder implements ItemMeta.Builder {
        private final ItemComponentPatch.Builder components;
        private TagHandler tagHandler = null;

        Builder(ItemComponentPatch.Builder components) {
            this.components = components;
        }

        @Override
        public ItemMeta.@NotNull Builder damage(int damage) {
            components.set(ItemComponent.DAMAGE, damage);
            return this;
        }

        @Override
        public ItemMeta.@NotNull Builder unbreakable(boolean unbreakable) {
            if (unbreakable) {
                components.set(ItemComponent.UNBREAKABLE, new Unbreakable(
                        components.get(ItemComponent.UNBREAKABLE, Unbreakable.DEFAULT).showInTooltip()));
            } else {
                components.remove(ItemComponent.UNBREAKABLE);
            }
            return this;
        }

        @Override
        public ItemMeta.@NotNull Builder hideFlag(int hideFlag) {
            return this; //todo
        }

        @Override
        public ItemMeta.@NotNull Builder displayName(@Nullable Component displayName) {
            if (displayName == null) {
                components.remove(ItemComponent.CUSTOM_NAME);
            } else {
                components.set(ItemComponent.CUSTOM_NAME, displayName);
            }
            return this;
        }

        @Override
        public ItemMeta.@NotNull Builder lore(@NotNull List<? extends Component> lore) {
            components.set(ItemComponent.LORE, new ArrayList<>(lore));
            return this;
        }

        @Override
        public ItemMeta.@NotNull Builder enchantments(@NotNull Map<Enchantment, Short> enchantments) {
            EnchantmentList existing = components.get(ItemComponent.ENCHANTMENTS, EnchantmentList.EMPTY);
            Map<Enchantment, Integer> map = new HashMap<>(enchantments.size());
            for (Map.Entry<Enchantment, Short> entry : enchantments.entrySet()) {
                map.put(entry.getKey(), (int) entry.getValue());
            }
            components.set(ItemComponent.ENCHANTMENTS, new EnchantmentList(map, existing.showInTooltip()));
            return this;
        }

        @Override
        public ItemMeta.@NotNull Builder enchantment(@NotNull Enchantment enchantment, short level) {
            components.set(ItemComponent.ENCHANTMENTS, components.get(ItemComponent.ENCHANTMENTS, EnchantmentList.EMPTY)
                    .with(enchantment, level));
            return this;
        }

        @Override
        public ItemMeta.@NotNull Builder attributes(@NotNull List<@NotNull ItemAttribute> attributes) {
            return this; //todo
        }

        @Override
        public ItemMeta.@NotNull Builder customModelData(int customModelData) {
            components.set(ItemComponent.CUSTOM_MODEL_DATA, customModelData);
            return this;
        }

        @Override
        public ItemMeta.@NotNull Builder canPlaceOn(@NotNull Set<@NotNull Block> blocks) {
            //todo
            return this;
        }

        @Override
        public ItemMeta.@NotNull Builder canDestroy(@NotNull Set<@NotNull Block> blocks) {
            //todo
            return this;
        }

        @Override
        public @NotNull TagHandler tagHandler() {
            this.tagHandler = TagHandler.fromCompound(components.get(ItemComponent.CUSTOM_DATA, CustomData.EMPTY).nbt());
            return tagHandler;
        }

        @Override
        public @NotNull ItemMetaImpl build() {
            if (tagHandler != null) {
                // If tagHandler was called then a tag was probably changed so update custom data.
                components.set(ItemComponent.CUSTOM_DATA, new CustomData(tagHandler.asCompound()));
            }
            return new ItemMetaImpl(components.build());
        }

    }
}
