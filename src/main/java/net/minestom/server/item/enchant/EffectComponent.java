package net.minestom.server.item.enchant;

import net.kyori.adventure.key.Key;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.item.crossbow.CrossbowChargingSounds;
import net.minestom.server.registry.ObjectSet;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.Unit;
import net.minestom.server.utils.collection.ObjectArray;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EffectComponent {
    static final Map<String, DataComponent<?>> KEYS = new HashMap<>(32);
    static final ObjectArray<DataComponent<?>> IDS = ObjectArray.singleThread(32);

    public static final DataComponent<List<ConditionalEffect<ValueEffect>>> DAMAGE_PROTECTION = register("damage_protection", ConditionalEffect.nbtType(ValueEffect.NBT_TYPE).list());
    public static final DataComponent<List<ConditionalEffect<DamageImmunityEffect>>> DAMAGE_IMMUNITY = register("damage_immunity", ConditionalEffect.nbtType(DamageImmunityEffect.NBT_TYPE).list());
    public static final DataComponent<List<ConditionalEffect<ValueEffect>>> DAMAGE = register("damage", ConditionalEffect.nbtType(ValueEffect.NBT_TYPE).list());
    public static final DataComponent<List<ConditionalEffect<ValueEffect>>> SMASH_DAMAGE_PER_FALLEN_BLOCK = register("smash_damage_per_fallen_block", ConditionalEffect.nbtType(ValueEffect.NBT_TYPE).list());
    public static final DataComponent<List<ConditionalEffect<ValueEffect>>> KNOCKBACK = register("knockback", ConditionalEffect.nbtType(ValueEffect.NBT_TYPE).list());
    public static final DataComponent<List<ConditionalEffect<ValueEffect>>> ARMOR_EFFECTIVENESS = register("armor_effectiveness", ConditionalEffect.nbtType(ValueEffect.NBT_TYPE).list());
    public static final DataComponent<List<TargetedConditionalEffect<EntityEffect>>> POST_ATTACK = register("post_attack", TargetedConditionalEffect.nbtType(EntityEffect.NBT_TYPE).list());
    public static final DataComponent<List<ConditionalEffect<EntityEffect>>> HIT_BLOCK = register("hit_block", ConditionalEffect.nbtType(EntityEffect.NBT_TYPE).list());
    public static final DataComponent<List<ConditionalEffect<ValueEffect>>> ITEM_DAMAGE = register("item_damage", ConditionalEffect.nbtType(ValueEffect.NBT_TYPE).list());
    public static final DataComponent<List<AttributeEffect>> ATTRIBUTES = register("attributes", AttributeEffect.NBT_TYPE.list());
    public static final DataComponent<List<TargetedConditionalEffect<ValueEffect>>> EQUIPMENT_DROPS = register("equipment_drops", TargetedConditionalEffect.nbtType(ValueEffect.NBT_TYPE).list());
    public static final DataComponent<List<ConditionalEffect<LocationEffect>>> LOCATION_CHANGED = register("location_changed", ConditionalEffect.nbtType(LocationEffect.NBT_TYPE).list());
    public static final DataComponent<List<ConditionalEffect<EntityEffect>>> TICK = register("tick", ConditionalEffect.nbtType(EntityEffect.NBT_TYPE).list());
    public static final DataComponent<List<ConditionalEffect<ValueEffect>>> AMMO_USE = register("ammo_use", ConditionalEffect.nbtType(ValueEffect.NBT_TYPE).list());
    public static final DataComponent<List<ConditionalEffect<ValueEffect>>> PROJECTILE_PIERCING = register("projectile_piercing", ConditionalEffect.nbtType(ValueEffect.NBT_TYPE).list());
    public static final DataComponent<List<ConditionalEffect<EntityEffect>>> PROJECTILE_SPAWNED = register("projectile_spawned", ConditionalEffect.nbtType(EntityEffect.NBT_TYPE).list());
    public static final DataComponent<List<ConditionalEffect<ValueEffect>>> PROJECTILE_SPREAD = register("projectile_spread", ConditionalEffect.nbtType(ValueEffect.NBT_TYPE).list());
    public static final DataComponent<List<ConditionalEffect<ValueEffect>>> PROJECTILE_COUNT = register("projectile_count", ConditionalEffect.nbtType(ValueEffect.NBT_TYPE).list());
    public static final DataComponent<List<ConditionalEffect<ValueEffect>>> TRIDENT_RETURN_ACCELERATION = register("trident_return_acceleration", ConditionalEffect.nbtType(ValueEffect.NBT_TYPE).list());
    public static final DataComponent<List<ConditionalEffect<ValueEffect>>> FISHING_TIME_REDUCTION = register("fishing_time_reduction", ConditionalEffect.nbtType(ValueEffect.NBT_TYPE).list());
    public static final DataComponent<List<ConditionalEffect<ValueEffect>>> FISHING_LUCK_BONUS = register("fishing_luck_bonus", ConditionalEffect.nbtType(ValueEffect.NBT_TYPE).list());
    public static final DataComponent<List<ConditionalEffect<ValueEffect>>> BLOCK_EXPERIENCE = register("block_experience", ConditionalEffect.nbtType(ValueEffect.NBT_TYPE).list());
    public static final DataComponent<List<ConditionalEffect<ValueEffect>>> MOB_EXPERIENCE = register("mob_experience", ConditionalEffect.nbtType(ValueEffect.NBT_TYPE).list());
    public static final DataComponent<List<ConditionalEffect<ValueEffect>>> REPAIR_WITH_XP = register("repair_with_xp", ConditionalEffect.nbtType(ValueEffect.NBT_TYPE).list());
    public static final DataComponent<ValueEffect> CROSSBOW_CHARGE_TIME = register("crossbow_charge_time", ValueEffect.NBT_TYPE);
    public static final DataComponent<List<CrossbowChargingSounds>> CROSSBOW_CHARGING_SOUNDS = register("crossbow_charging_sounds", CrossbowChargingSounds.NBT_TYPE.list());
    public static final DataComponent<List<ObjectSet<SoundEvent>>> TRIDENT_SOUND = register("trident_sound", ObjectSet.<SoundEvent>nbtType(Tag.BasicType.SOUND_EVENTS).list());
    public static final DataComponent<Unit> PREVENT_EQUIPMENT_DROP = register("prevent_equipment_drop", BinaryTagSerializer.UNIT);
    public static final DataComponent<Unit> PREVENT_ARMOR_CHANGE = register("prevent_armor_change", BinaryTagSerializer.UNIT);
    public static final DataComponent<ValueEffect> TRIDENT_SPIN_ATTACK_STRENGTH = register("trident_spin_attack_strength", ValueEffect.NBT_TYPE);

    public static final BinaryTagSerializer<DataComponentMap> MAP_NBT_TYPE = DataComponentMap.nbtType(EffectComponent::fromId, EffectComponent::fromKey);

    public static @Nullable DataComponent<?> fromKey(@NotNull String key) {
        return KEYS.get(key);
    }

    public static @Nullable DataComponent<?> fromKey(@NotNull Key key) {
        return fromKey(key.asString());
    }

    /**
     * @deprecated use {@link #fromKey(String)}
     */
    @Deprecated
    static DataComponent<?> fromNamespaceId(@NotNull String namespaceID) {
        return fromKey(namespaceID);
    }

    /**
     * @deprecated use {@link #fromKey(Key)}
     */
    @Deprecated
    static DataComponent<?> fromNamespaceId(@NotNull NamespaceID namespaceID) {
        return fromKey(namespaceID);
    }


    public static @Nullable DataComponent<?> fromId(int id) {
        return IDS.get(id);
    }

    public static @NotNull Collection<DataComponent<?>> values() {
        return KEYS.values();
    }

    static <T> DataComponent<T> register(@NotNull String name, @Nullable BinaryTagSerializer<T> nbt) {
        DataComponent<T> impl = DataComponent.createHeadless(KEYS.size(), Key.key(name), null, nbt);
        KEYS.put(impl.name(), impl);
        IDS.set(impl.id(), impl);
        return impl;
    }
}
