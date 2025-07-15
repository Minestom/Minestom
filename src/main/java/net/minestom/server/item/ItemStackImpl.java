package net.minestom.server.item;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.component.CustomData;
import net.minestom.server.item.component.TooltipDisplay;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.RegistryTranscoder;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.function.Consumer;

record ItemStackImpl(Material material, int amount, DataComponentMap components) implements ItemStack {

    static NetworkBuffer.Type<ItemStack> networkType(NetworkBuffer.Type<DataComponentMap> componentPatchType) {
        return new NetworkBuffer.Type<>() {
            @Override
            public void write(NetworkBuffer buffer, ItemStack value) {
                if (value.isAir()) {
                    buffer.write(NetworkBuffer.VAR_INT, 0);
                    return;
                }

                buffer.write(NetworkBuffer.VAR_INT, value.amount());
                buffer.write(NetworkBuffer.VAR_INT, value.material().id());
                buffer.write(componentPatchType, ((ItemStackImpl) value).components());
            }

            @Override
            public ItemStack read(NetworkBuffer buffer) {
                int amount = buffer.read(NetworkBuffer.VAR_INT);
                if (amount <= 0) return ItemStack.AIR;
                Material material = Material.fromId(buffer.read(NetworkBuffer.VAR_INT));
                DataComponentMap components = buffer.read(componentPatchType);
                return ItemStackImpl.create(material, amount, components);
            }
        };
    }

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
    public DataComponentMap componentPatch() {
        return this.components;
    }

    @Override
    public <T> @Nullable T get(DataComponent<T> component) {
        return components.get(material.prototype(), component);
    }

    @Override
    public boolean has(DataComponent<?> component) {
        return components.has(material.prototype(), component);
    }

    @Override
    public ItemStack with(Consumer<ItemStack.Builder> consumer) {
        ItemStack.Builder builder = builder();
        consumer.accept(builder);
        return builder.build();
    }

    @Override
    public ItemStack withMaterial(Material material) {
        return new ItemStackImpl(material, amount, components);
    }

    @Override
    public ItemStack withAmount(int amount) {
        if (amount <= 0) return ItemStack.AIR;
        return create(material, amount, components);
    }

    @Override
    public <T> ItemStack with(DataComponent<T> component, T value) {
        return new ItemStackImpl(material, amount, components.set(component, value));
    }

    @Override
    public ItemStack without(DataComponent<?> component) {
        // We can be slightly smart here. If the component is not present, this will always be a noop.
        // No need to make a new patch with the removal only for it to be removed again when doing a diff.
        if (get(component) == null) return this;
        return new ItemStackImpl(material, amount, components.remove(component));
    }

    @Override
    public ItemStack consume(int amount) {
        return withAmount(amount() - amount);
    }

    @Override
    public ItemStack damage(int amount) {
        final Integer damage = get(DataComponents.DAMAGE);
        if (damage == null) return this;
        final Integer maxDamage = get(DataComponents.MAX_DAMAGE);
        if (maxDamage != null && damage + amount >= maxDamage) {
            return ItemStack.AIR;
        } else {
            return with(DataComponents.DAMAGE, damage + amount);
        }
    }

    @Override
    public boolean isSimilar(ItemStack itemStack) {
        return material == itemStack.material() && components.equals(((ItemStackImpl) itemStack).components);
    }

    @Override
    public CompoundBinaryTag toItemNBT() {
        final Transcoder<BinaryTag> coder = new RegistryTranscoder<>(Transcoder.NBT, MinecraftServer.process());
        return (CompoundBinaryTag) CODEC.encode(coder, this).orElseThrow("Invalid NBT for ItemStack");
    }

    @Override
    @Contract(value = "-> new", pure = true)
    public ItemStack.Builder builder() {
        return new Builder(material, amount, components.toPatchBuilder());
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
        public ItemStack.Builder material(Material material) {
            this.material = material;
            return this;
        }

        @Override
        public ItemStack.Builder amount(int amount) {
            this.amount = amount;
            return this;
        }

        @Override
        public <T> ItemStack.Builder set(DataComponent<T> component, T value) {
            components.set(component, value);
            return this;
        }

        @Override
        public ItemStack.Builder remove(DataComponent<?> component) {
            components.remove(component);
            return this;
        }

        @Override
        public <T> ItemStack.Builder set(Tag<T> tag, @Nullable T value) {
            components.set(DataComponents.CUSTOM_DATA, components.get(DataComponents.CUSTOM_DATA, CustomData.EMPTY).withTag(tag, value));
            return this;
        }

        @Override
        public ItemStack.Builder hideExtraTooltip() {
            return set(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(false, Set.of(
                    DataComponents.BANNER_PATTERNS, DataComponents.BEES, DataComponents.BLOCK_ENTITY_DATA,
                    DataComponents.BLOCK_STATE, DataComponents.BUNDLE_CONTENTS, DataComponents.CHARGED_PROJECTILES,
                    DataComponents.CONTAINER, DataComponents.CONTAINER_LOOT, DataComponents.FIREWORK_EXPLOSION,
                    DataComponents.FIREWORKS, DataComponents.INSTRUMENT, DataComponents.MAP_ID,
                    DataComponents.PAINTING_VARIANT, DataComponents.POT_DECORATIONS, DataComponents.POTION_CONTENTS,
                    DataComponents.TROPICAL_FISH_PATTERN, DataComponents.WRITTEN_BOOK_CONTENT
            )));
        }

        @Override
        public ItemStack build() {
            return ItemStackImpl.create(material, amount, components.build());
        }

    }
}
