package net.minestom.server.item;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.component.*;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

record ItemStackImpl(Material material, int amount, DataComponentMap components) implements ItemStack {

    static ItemStack create(Material material, int amount, DataComponentMap components) {
        if (amount <= 0) return AIR;
        return new ItemStackImpl(material, amount, components);
    }

    static ItemStack create(Material material, int amount) {
        return create(material, amount, DataComponentMap.EMPTY);
    }

    public ItemStackImpl {
        Check.notNull(material, "Material cannot be null");

        // It is relevant to create the minimal diff of the prototype so that #isSimilar returns consistent
        // results for ItemStacks which would resolve to the same thing. For example, consider two items
        // (name indicating prototype, brackets showing the components given during construction):
        // 1: apple[max_stack_size=64, custom_name=Hello]
        // 2: apple[custom_name=Hello]
        // After resolution the first set of components would turn into the second one because apple already has a
        // max stack size of 64. If we did not do this, #isSimilar would return false for these two items because of
        // their different patches.
        // It is worth noting that the client would handle both cases perfectly fine.
        if (components != DataComponentMap.EMPTY) {
            components = DataComponentMap.diff(material.prototype(), components);
        }
    }

    @Override
    public @NotNull DataComponentMap componentPatch() {
        return this.components;
    }

    @Override
    public <T> @Nullable T get(@NotNull DataComponent<T> component) {
        return components.get(material.prototype(), component);
    }

    @Override
    public boolean has(@NotNull DataComponent<?> component) {
        return components.has(material.prototype(), component);
    }

    @Override
    public @NotNull ItemStack with(@NotNull Consumer<ItemStack.@NotNull Builder> consumer) {
        ItemStack.Builder builder = builder();
        consumer.accept(builder);
        return builder.build();
    }

    @Override
    public @NotNull ItemStack withMaterial(@NotNull Material material) {
        return new ItemStackImpl(material, amount, components);
    }

    @Override
    public @NotNull ItemStack withAmount(int amount) {
        if (amount <= 0) return ItemStack.AIR;
        return create(material, amount, components);
    }

    @Override
    public @NotNull <T> ItemStack with(@NotNull DataComponent<T> component, @NotNull T value) {
        return new ItemStackImpl(material, amount, components.set(component, value));
    }

    @Override
    public @NotNull ItemStack without(@NotNull DataComponent<?> component) {
        // We can be slightly smart here. If the component is not present, this will always be a noop.
        // No need to make a new patch with the removal only for it to be removed again when doing a diff.
        if (get(component) == null) return this;
        return new ItemStackImpl(material, amount, components.remove(component));
    }

    @Override
    public @NotNull ItemStack consume(int amount) {
        return withAmount(amount() - amount);
    }

    @Override
    public boolean isSimilar(@NotNull ItemStack itemStack) {
        return material == itemStack.material() && components.equals(((ItemStackImpl) itemStack).components);
    }

    @Override
    public @NotNull CompoundBinaryTag toItemNBT() {
        return (CompoundBinaryTag) NBT_TYPE.write(this);
    }

    @Override
    @Contract(value = "-> new", pure = true)
    public @NotNull ItemStack.Builder builder() {
        return new Builder(material, amount, components.toPatchBuilder());
    }

    static @NotNull ItemStack fromCompound(@NotNull CompoundBinaryTag tag) {
        String id = tag.getString("id");
        Material material = Material.fromKey(id);
        Check.notNull(material, "Unknown material: {0}", id);
        int count = tag.getInt("count", 1);

        BinaryTagSerializer.Context context = new BinaryTagSerializer.ContextWithRegistries(MinecraftServer.process(), false);
        DataComponentMap patch = DataComponent.PATCH_NBT_TYPE.read(context, tag.getCompound("components"));
        return new ItemStackImpl(material, count, patch);
    }

    static @NotNull CompoundBinaryTag toCompound(@NotNull ItemStack itemStack) {
        CompoundBinaryTag.Builder tag = CompoundBinaryTag.builder();
        tag.putString("id", itemStack.material().name());
        tag.putInt("count", itemStack.amount());

        BinaryTagSerializer.Context context = new BinaryTagSerializer.ContextWithRegistries(MinecraftServer.process(), false);
        CompoundBinaryTag components = (CompoundBinaryTag) DataComponent.PATCH_NBT_TYPE.write(context, ((ItemStackImpl) itemStack).components);
        if (components.size() > 0) tag.put("components", components);

        return tag.build();
    }

    static final class Builder implements ItemStack.Builder {
        private Material material;
        private int amount;
        private DataComponentMap.PatchBuilder components;

        Builder(Material material, int amount, DataComponentMap.PatchBuilder components) {
            this.material = material;
            this.amount = amount;
            this.components = components;
        }

        Builder(Material material, int amount) {
            this.material = material;
            this.amount = amount;
            this.components = DataComponentMap.patchBuilder();
        }

        @Override
        public ItemStack.@NotNull Builder material(@NotNull Material material) {
            this.material = material;
            return this;
        }

        @Override
        public ItemStack.@NotNull Builder amount(int amount) {
            this.amount = amount;
            return this;
        }

        @Override
        public <T> ItemStack.@NotNull Builder set(@NotNull DataComponent<T> component, T value) {
            components.set(component, value);
            return this;
        }

        @Override
        public ItemStack.@NotNull Builder remove(@NotNull DataComponent<?> component) {
            components.remove(component);
            return this;
        }

        @Override
        public <T> ItemStack.@NotNull Builder set(@NotNull Tag<T> tag, @Nullable T value) {
            components.set(DataComponents.CUSTOM_DATA, components.get(DataComponents.CUSTOM_DATA, CustomData.EMPTY).withTag(tag, value));
            return this;
        }

        @Override
        public ItemStack.@NotNull Builder hideExtraTooltip() {
            // TODO(1.21.5)
//            AttributeList attributeModifiers = components.get(DataComponents.ATTRIBUTE_MODIFIERS);
//            components.set(DataComponents.ATTRIBUTE_MODIFIERS, attributeModifiers == null
//                    ? new AttributeList(List.of(), false) : attributeModifiers.withTooltip(false));
//            Unbreakable unbreakable = components.get(DataComponents.UNBREAKABLE);
//            if (unbreakable != null) components.set(DataComponents.UNBREAKABLE, new Unbreakable(false));
//            ArmorTrim armorTrim = components.get(DataComponents.TRIM);
//            if (armorTrim != null) components.set(DataComponents.TRIM, armorTrim.withTooltip(false));
//            BlockPredicates canBreak = components.get(DataComponents.CAN_BREAK);
//            if (canBreak != null) components.set(DataComponents.CAN_BREAK, canBreak.withTooltip(false));
//            BlockPredicates canPlaceOn = components.get(DataComponents.CAN_PLACE_ON);
//            if (canPlaceOn != null) components.set(DataComponents.CAN_PLACE_ON, canPlaceOn.withTooltip(false));
//            DyedItemColor dyedColor = components.get(DataComponents.DYED_COLOR);
//            if (dyedColor != null) components.set(DataComponents.DYED_COLOR, dyedColor.withTooltip(false));
//            EnchantmentList enchantments = components.get(DataComponents.ENCHANTMENTS);
//            if (enchantments != null) components.set(DataComponents.ENCHANTMENTS, enchantments.withTooltip(false));
//            JukeboxPlayable jukeboxPlayable = components.get(DataComponents.JUKEBOX_PLAYABLE);
//            if (jukeboxPlayable != null)
//                components.set(DataComponents.JUKEBOX_PLAYABLE, jukeboxPlayable.withTooltip(false));
//            return set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
            // TODO(1.21.5)
            throw new RuntimeException("todo");
        }

        @Override
        public @NotNull ItemStack build() {
            return ItemStackImpl.create(material, amount, components.build());
        }

    }
}
