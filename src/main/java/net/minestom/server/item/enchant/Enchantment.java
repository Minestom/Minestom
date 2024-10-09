package net.minestom.server.item.enchant;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.entity.EquipmentSlotGroup;
import net.minestom.server.item.Material;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.*;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public sealed interface Enchantment extends ProtocolObject, Enchantments permits EnchantmentImpl {
    @NotNull NetworkBuffer.Type<DynamicRegistry.Key<Enchantment>> NETWORK_TYPE = NetworkBuffer.RegistryKey(Registries::enchantment);
    @NotNull BinaryTagSerializer<DynamicRegistry.Key<Enchantment>> NBT_TYPE = BinaryTagSerializer.registryKey(Registries::enchantment);

    static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * <p>Creates a new registry for enchantments, loading the vanilla enchantments.</p>
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static @NotNull DynamicRegistry<Enchantment> createDefaultRegistry(@NotNull Registries registries) {
        return DynamicRegistry.create(
                // TODO(1.21.2) new enchantment types
                "minecraft:enchantment", EnchantmentImpl.REGISTRY_NBT_TYPE//,
                //registries, Registry.Resource.ENCHANTMENTS
        );
    }

    @NotNull Component description();

    @NotNull ObjectSet<Enchantment> exclusiveSet();

    @NotNull ObjectSet<Material> supportedItems();

    @NotNull ObjectSet<Material> primaryItems();

    int weight();

    int maxLevel();

    @NotNull Cost minCost();

    @NotNull Cost maxCost();

    int anvilCost();

    @NotNull List<EquipmentSlotGroup> slots();

    @NotNull DataComponentMap effects();

    @Override
    @Nullable Registry.EnchantmentEntry registry();

    enum Target {
        ATTACKER,
        DAMAGING_ENTITY,
        VICTIM;

        public static final BinaryTagSerializer<Target> NBT_TYPE = BinaryTagSerializer.fromEnumStringable(Target.class);
    }

    sealed interface Effect permits AttributeEffect, ConditionalEffect, DamageImmunityEffect, EntityEffect, LocationEffect, TargetedConditionalEffect, ValueEffect {

    }

    record Cost(int base, int perLevelAboveFirst) {
        public static final Cost DEFAULT = new Cost(1, 1);

        public static final BinaryTagSerializer<Cost> NBT_TYPE = BinaryTagSerializer.COMPOUND.map(
                tag -> new Cost(tag.getInt("base"), tag.getInt("per_level_above_first")),
                cost -> CompoundBinaryTag.builder()
                        .putInt("base", cost.base)
                        .putInt("per_level_above_first", cost.perLevelAboveFirst)
                        .build()
        );
    }

    class Builder {
        private Component description = Component.empty();
        private ObjectSet<Enchantment> exclusiveSet = ObjectSet.empty();
        private ObjectSet<Material> supportedItems = ObjectSet.empty();
        private ObjectSet<Material> primaryItems = ObjectSet.empty();
        private int weight = 1;
        private int maxLevel = 1;
        private Cost minCost = Cost.DEFAULT;
        private Cost maxCost = Cost.DEFAULT;
        private int anvilCost = 0;
        private List<EquipmentSlotGroup> slots = List.of();
        private DataComponentMap.Builder effects = DataComponentMap.builder();

        private Builder() {
        }

        public @NotNull Builder description(@NotNull Component description) {
            this.description = description;
            return this;
        }

        public @NotNull Builder exclusiveSet(@NotNull ObjectSet<Enchantment> exclusiveSet) {
            this.exclusiveSet = exclusiveSet;
            return this;
        }

        public @NotNull Builder supportedItems(@NotNull ObjectSet<Material> supportedItems) {
            this.supportedItems = supportedItems;
            return this;
        }

        public @NotNull Builder primaryItems(@NotNull ObjectSet<Material> primaryItems) {
            this.primaryItems = primaryItems;
            return this;
        }

        public @NotNull Builder weight(int weight) {
            this.weight = weight;
            return this;
        }

        public @NotNull Builder maxLevel(int maxLevel) {
            this.maxLevel = maxLevel;
            return this;
        }

        public @NotNull Builder minCost(int base, int perLevelAboveFirst) {
            return minCost(new Cost(base, perLevelAboveFirst));
        }

        public @NotNull Builder minCost(@NotNull Cost minCost) {
            this.minCost = minCost;
            return this;
        }

        public @NotNull Builder maxCost(int base, int perLevelAboveFirst) {
            return maxCost(new Cost(base, perLevelAboveFirst));
        }

        public @NotNull Builder maxCost(@NotNull Cost maxCost) {
            this.maxCost = maxCost;
            return this;
        }

        public @NotNull Builder anvilCost(int anvilCost) {
            this.anvilCost = anvilCost;
            return this;
        }

        public @NotNull Builder slots(@NotNull EquipmentSlotGroup... slots) {
            this.slots = List.of(slots);
            return this;
        }

        public @NotNull Builder slots(@NotNull List<EquipmentSlotGroup> slots) {
            this.slots = slots;
            return this;
        }

        public <T> @NotNull Builder effect(@NotNull DataComponent<T> component, @NotNull T value) {
            effects.set(component, value);
            return this;
        }

        public @NotNull Builder effects(@NotNull DataComponentMap effects) {
            this.effects = effects.toBuilder();
            return this;
        }

        public @NotNull Enchantment build() {
            return new EnchantmentImpl(
                    description, exclusiveSet, supportedItems,
                    primaryItems, weight, maxLevel, minCost, maxCost,
                    anvilCost, slots, effects.build(), null
            );
        }
    }

}
