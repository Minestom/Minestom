package net.minestom.server.item.enchant;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.entity.EquipmentSlotGroup;
import net.minestom.server.item.Material;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.*;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.List;

public sealed interface Enchantment extends Enchantments permits EnchantmentImpl {
    NetworkBuffer.Type<RegistryKey<Enchantment>> NETWORK_TYPE = RegistryKey.networkType(Registries::enchantment);
    Codec<RegistryKey<Enchantment>> CODEC = RegistryKey.codec(Registries::enchantment);

    Codec<Enchantment> REGISTRY_CODEC = StructCodec.struct(
            "description", Codec.COMPONENT, Enchantment::description,
            "exclusive_set", RegistryTag.codec(Registries::enchantment).optional(RegistryTag.empty()), Enchantment::exclusiveSet,
            "supported_items", RegistryTag.codec(Registries::material), Enchantment::supportedItems,
            "primary_items", RegistryTag.codec(Registries::material).optional(), Enchantment::primaryItems,
            "weight", Codec.INT, Enchantment::weight,
            "max_level", Codec.INT, Enchantment::maxLevel,
            "min_cost", Cost.CODEC, Enchantment::minCost,
            "max_cost", Cost.CODEC, Enchantment::maxCost,
            "anvil_cost", Codec.INT, Enchantment::anvilCost,
            "slots", EquipmentSlotGroup.CODEC.list(), Enchantment::slots,
            "effects", EffectComponent.CODEC.optional(DataComponentMap.EMPTY), Enchantment::effects,
            EnchantmentImpl::new);

    static Builder builder() {
        return new Builder();
    }

    /**
     * <p>Creates a new registry for enchantments, loading the vanilla enchantments.</p>
     *
     * @see net.minestom.server.MinecraftServer to get an existing instance of the registry
     */
    @ApiStatus.Internal
    static DynamicRegistry<Enchantment> createDefaultRegistry(Registries registries) {
        return DynamicRegistry.createForEnchantmentsWithSelfReferentialLoadingNightmare(
                Key.key("minecraft:enchantment"), REGISTRY_CODEC, RegistryData.Resource.ENCHANTMENTS, registries
        );
    }

    Component description();

    RegistryTag<Enchantment> exclusiveSet();

    RegistryTag<Material> supportedItems();

    @Nullable RegistryTag<Material> primaryItems();

    int weight();

    int maxLevel();

    Cost minCost();

    Cost maxCost();

    int anvilCost();

    List<EquipmentSlotGroup> slots();

    DataComponentMap effects();

    enum Target {
        ATTACKER,
        DAMAGING_ENTITY,
        VICTIM;

        public static final Codec<Target> CODEC = Codec.Enum(Target.class);
    }

    sealed interface Effect permits AttributeEffect, ConditionalEffect, DamageImmunityEffect, EntityEffect, LocationEffect, TargetedConditionalEffect, ValueEffect {

    }

    record Cost(int base, int perLevelAboveFirst) {
        public static final Cost DEFAULT = new Cost(1, 1);

        public static final Codec<Cost> CODEC = StructCodec.struct(
                "base", Codec.INT, Cost::base,
                "per_level_above_first", Codec.INT, Cost::perLevelAboveFirst,
                Cost::new);
    }

    class Builder {
        private Component description = Component.empty();
        private RegistryTag<Enchantment> exclusiveSet = RegistryTag.empty();
        private RegistryTag<Material> supportedItems = RegistryTag.empty();
        private RegistryTag<Material> primaryItems = RegistryTag.empty();
        private int weight = 1;
        private int maxLevel = 1;
        private Cost minCost = Cost.DEFAULT;
        private Cost maxCost = Cost.DEFAULT;
        private int anvilCost = 0;
        private List<EquipmentSlotGroup> slots = List.of();
        private DataComponentMap.Builder effects = DataComponentMap.builder();

        private Builder() {
        }

        public Builder description(Component description) {
            this.description = description;
            return this;
        }

        public Builder exclusiveSet(RegistryTag<Enchantment> exclusiveSet) {
            this.exclusiveSet = exclusiveSet;
            return this;
        }

        public Builder supportedItems(RegistryTag<Material> supportedItems) {
            this.supportedItems = supportedItems;
            return this;
        }

        public Builder primaryItems(RegistryTag<Material> primaryItems) {
            this.primaryItems = primaryItems;
            return this;
        }

        public Builder weight(int weight) {
            this.weight = weight;
            return this;
        }

        public Builder maxLevel(int maxLevel) {
            this.maxLevel = maxLevel;
            return this;
        }

        public Builder minCost(int base, int perLevelAboveFirst) {
            return minCost(new Cost(base, perLevelAboveFirst));
        }

        public Builder minCost(Cost minCost) {
            this.minCost = minCost;
            return this;
        }

        public Builder maxCost(int base, int perLevelAboveFirst) {
            return maxCost(new Cost(base, perLevelAboveFirst));
        }

        public Builder maxCost(Cost maxCost) {
            this.maxCost = maxCost;
            return this;
        }

        public Builder anvilCost(int anvilCost) {
            this.anvilCost = anvilCost;
            return this;
        }

        public Builder slots(EquipmentSlotGroup... slots) {
            this.slots = List.of(slots);
            return this;
        }

        public Builder slots(List<EquipmentSlotGroup> slots) {
            this.slots = slots;
            return this;
        }

        public <T> Builder effect(DataComponent<T> component, T value) {
            effects.set(component, value);
            return this;
        }

        public Builder effects(DataComponentMap effects) {
            this.effects = effects.toBuilder();
            return this;
        }

        public Enchantment build() {
            return new EnchantmentImpl(
                    description, exclusiveSet, supportedItems,
                    primaryItems, weight, maxLevel, minCost, maxCost,
                    anvilCost, slots, effects.build()
            );
        }
    }

}
