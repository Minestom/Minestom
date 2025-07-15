package net.minestom.server.item.enchant;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.item.crossbow.CrossbowChargingSounds;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.Unit;
import net.minestom.server.utils.collection.ObjectArray;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EffectComponent {
    static final Map<String, DataComponent<?>> NAMESPACES = new HashMap<>(32);
    static final ObjectArray<DataComponent<?>> IDS = ObjectArray.singleThread(32);

    public static final DataComponent<List<ConditionalEffect<ValueEffect>>> DAMAGE_PROTECTION = register("damage_protection", ConditionalEffect.codec(ValueEffect.CODEC).list());
    public static final DataComponent<List<ConditionalEffect<DamageImmunityEffect>>> DAMAGE_IMMUNITY = register("damage_immunity", ConditionalEffect.codec(DamageImmunityEffect.CODEC).list());
    public static final DataComponent<List<ConditionalEffect<ValueEffect>>> DAMAGE = register("damage", ConditionalEffect.codec(ValueEffect.CODEC).list());
    public static final DataComponent<List<ConditionalEffect<ValueEffect>>> SMASH_DAMAGE_PER_FALLEN_BLOCK = register("smash_damage_per_fallen_block", ConditionalEffect.codec(ValueEffect.CODEC).list());
    public static final DataComponent<List<ConditionalEffect<ValueEffect>>> KNOCKBACK = register("knockback", ConditionalEffect.codec(ValueEffect.CODEC).list());
    public static final DataComponent<List<ConditionalEffect<ValueEffect>>> ARMOR_EFFECTIVENESS = register("armor_effectiveness", ConditionalEffect.codec(ValueEffect.CODEC).list());
    public static final DataComponent<List<TargetedConditionalEffect<EntityEffect>>> POST_ATTACK = register("post_attack", TargetedConditionalEffect.nbtType(EntityEffect.CODEC).list());
    public static final DataComponent<List<ConditionalEffect<EntityEffect>>> HIT_BLOCK = register("hit_block", ConditionalEffect.codec(EntityEffect.CODEC).list());
    public static final DataComponent<List<ConditionalEffect<ValueEffect>>> ITEM_DAMAGE = register("item_damage", ConditionalEffect.codec(ValueEffect.CODEC).list());
    public static final DataComponent<List<AttributeEffect>> ATTRIBUTES = register("attributes", AttributeEffect.CODEC.list());
    public static final DataComponent<List<TargetedConditionalEffect<ValueEffect>>> EQUIPMENT_DROPS = register("equipment_drops", TargetedConditionalEffect.nbtType(ValueEffect.CODEC).list());
    public static final DataComponent<List<ConditionalEffect<LocationEffect>>> LOCATION_CHANGED = register("location_changed", ConditionalEffect.codec(LocationEffect.CODEC).list());
    public static final DataComponent<List<ConditionalEffect<EntityEffect>>> TICK = register("tick", ConditionalEffect.codec(EntityEffect.CODEC).list());
    public static final DataComponent<List<ConditionalEffect<ValueEffect>>> AMMO_USE = register("ammo_use", ConditionalEffect.codec(ValueEffect.CODEC).list());
    public static final DataComponent<List<ConditionalEffect<ValueEffect>>> PROJECTILE_PIERCING = register("projectile_piercing", ConditionalEffect.codec(ValueEffect.CODEC).list());
    public static final DataComponent<List<ConditionalEffect<EntityEffect>>> PROJECTILE_SPAWNED = register("projectile_spawned", ConditionalEffect.codec(EntityEffect.CODEC).list());
    public static final DataComponent<List<ConditionalEffect<ValueEffect>>> PROJECTILE_SPREAD = register("projectile_spread", ConditionalEffect.codec(ValueEffect.CODEC).list());
    public static final DataComponent<List<ConditionalEffect<ValueEffect>>> PROJECTILE_COUNT = register("projectile_count", ConditionalEffect.codec(ValueEffect.CODEC).list());
    public static final DataComponent<List<ConditionalEffect<ValueEffect>>> TRIDENT_RETURN_ACCELERATION = register("trident_return_acceleration", ConditionalEffect.codec(ValueEffect.CODEC).list());
    public static final DataComponent<List<ConditionalEffect<ValueEffect>>> FISHING_TIME_REDUCTION = register("fishing_time_reduction", ConditionalEffect.codec(ValueEffect.CODEC).list());
    public static final DataComponent<List<ConditionalEffect<ValueEffect>>> FISHING_LUCK_BONUS = register("fishing_luck_bonus", ConditionalEffect.codec(ValueEffect.CODEC).list());
    public static final DataComponent<List<ConditionalEffect<ValueEffect>>> BLOCK_EXPERIENCE = register("block_experience", ConditionalEffect.codec(ValueEffect.CODEC).list());
    public static final DataComponent<List<ConditionalEffect<ValueEffect>>> MOB_EXPERIENCE = register("mob_experience", ConditionalEffect.codec(ValueEffect.CODEC).list());
    public static final DataComponent<List<ConditionalEffect<ValueEffect>>> REPAIR_WITH_XP = register("repair_with_xp", ConditionalEffect.codec(ValueEffect.CODEC).list());
    public static final DataComponent<ValueEffect> CROSSBOW_CHARGE_TIME = register("crossbow_charge_time", ValueEffect.CODEC);
    public static final DataComponent<List<CrossbowChargingSounds>> CROSSBOW_CHARGING_SOUNDS = register("crossbow_charging_sounds", CrossbowChargingSounds.NBT_TYPE.list());
    public static final DataComponent<List<SoundEvent>> TRIDENT_SOUND = register("trident_sound", SoundEvent.CODEC.list());
    public static final DataComponent<Unit> PREVENT_EQUIPMENT_DROP = register("prevent_equipment_drop", Codec.UNIT);
    public static final DataComponent<Unit> PREVENT_ARMOR_CHANGE = register("prevent_armor_change", Codec.UNIT);
    public static final DataComponent<ValueEffect> TRIDENT_SPIN_ATTACK_STRENGTH = register("trident_spin_attack_strength", ValueEffect.CODEC);

    public static final Codec<DataComponentMap> CODEC = DataComponentMap.codec(EffectComponent::fromId, EffectComponent::fromNamespaceId);

    public static @Nullable DataComponent<?> fromNamespaceId(String namespaceId) {
        return NAMESPACES.get(namespaceId);
    }

    public static @Nullable DataComponent<?> fromKey(Key namespaceId) {
        return fromNamespaceId(namespaceId.asString());
    }

    public static @Nullable DataComponent<?> fromId(int id) {
        return IDS.get(id);
    }

    public static Collection<DataComponent<?>> values() {
        return NAMESPACES.values();
    }

    static <T> DataComponent<T> register(String name, @Nullable Codec<T> nbt) {
        DataComponent<T> impl = DataComponent.createHeadless(NAMESPACES.size(), Key.key(name), null, nbt);
        NAMESPACES.put(impl.name(), impl);
        IDS.set(impl.id(), impl);
        return impl;
    }
}
