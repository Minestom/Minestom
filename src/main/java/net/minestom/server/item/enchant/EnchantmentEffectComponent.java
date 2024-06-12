package net.minestom.server.item.enchant;

import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.Unit;
import net.minestom.server.utils.collection.ObjectArray;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EnchantmentEffectComponent {
    static final Map<String, DataComponent<?>> NAMESPACES = new HashMap<>(32);
    static final ObjectArray<DataComponent<?>> IDS = ObjectArray.singleThread(32);

    public static final DataComponent<Unit> DAMAGE_PROTECTION = register("damage_protection", null); //todo
    public static final DataComponent<Unit> DAMAGE_IMMUNITY = register("damage_immunity", null); //todo
    public static final DataComponent<Unit> DAMAGE = register("damage", null); //todo
    public static final DataComponent<Unit> SMASH_DAMAGE_PER_FALLEN_BLOCK = register("smash_damage_per_fallen_block", null); //todo
    public static final DataComponent<Unit> KNOCKBACK = register("knockback", null); //todo
    public static final DataComponent<Unit> ARMOR_EFFECTIVENESS = register("armor_effectiveness", null); //todo
    public static final DataComponent<Unit> POST_ATTACK = register("post_attack", null); //todo
    public static final DataComponent<Unit> HIT_BLOCK = register("hit_block", null); //todo
    public static final DataComponent<Unit> ITEM_DAMAGE = register("item_damage", null); //todo
    public static final DataComponent<Unit> ATTRIBUTES = register("attributes", null); //todo
    public static final DataComponent<Unit> EQUIPMENT_DROPS = register("equipment_drops", null); //todo
    public static final DataComponent<Unit> LOCATION_CHANGED = register("location_changed", null); //todo
    public static final DataComponent<Unit> TICK = register("tick", null); //todo
    public static final DataComponent<Unit> AMMO_USE = register("ammo_use", null); //todo
    public static final DataComponent<Unit> PROJECTILE_PIERCING = register("projectile_piercing", null); //todo
    public static final DataComponent<Unit> PROJECTILE_SPAWNED = register("projectile_spawned", null); //todo
    public static final DataComponent<Unit> PROJECTILE_SPREAD = register("projectile_spread", null); //todo
    public static final DataComponent<Unit> PROJECTILE_COUNT = register("projectile_count", null); //todo
    public static final DataComponent<Unit> TRIDENT_RETURN_ACCELERATION = register("trident_return_acceleration", null); //todo
    public static final DataComponent<Unit> FISHING_TIME_REDUCTION = register("fishing_time_reduction", null); //todo
    public static final DataComponent<Unit> FISHING_LUCK_BONUS = register("fishing_luck_bonus", null); //todo
    public static final DataComponent<Unit> BLOCK_EXPERIENCE = register("block_experience", null); //todo
    public static final DataComponent<Unit> MOB_EXPERIENCE = register("mob_experience", null); //todo
    public static final DataComponent<Unit> REPAIR_WITH_XP = register("repair_with_xp", null); //todo
    public static final DataComponent<Unit> CROSSBOW_CHARGE_TIME = register("crossbow_charge_time", null); //todo
    public static final DataComponent<Unit> CROSSBOW_CHARGING_SOUNDS = register("crossbow_charging_sounds", null); //todo
    public static final DataComponent<Unit> TRIDENT_SOUND = register("trident_sound", null); //todo
    public static final DataComponent<Unit> PREVENT_EQUIPMENT_DROP = register("prevent_equipment_drop", null); //todo
    public static final DataComponent<Unit> PREVENT_ARMOR_CHANGE = register("prevent_armor_change", null); //todo
    public static final DataComponent<Unit> TRIDENT_SPIN_ATTACK_STRENGTH = register("trident_spin_attack_strength", null); //todo

    private static final BinaryTagSerializer<DataComponent<?>> COMPONENT_NBT_TYPE = BinaryTagSerializer.STRING.map(EnchantmentEffectComponent::fromNamespaceId, DataComponent::name);

    public static final BinaryTagSerializer<DataComponentMap> NBT_TYPE = DataComponentMap.nbtType(COMPONENT_NBT_TYPE);

    public static @Nullable DataComponent<?> fromNamespaceId(@NotNull String namespaceId) {
        return NAMESPACES.get(namespaceId);
    }

    public static @Nullable DataComponent<?> fromNamespaceId(@NotNull NamespaceID namespaceId) {
        return fromNamespaceId(namespaceId.asString());
    }

    public static @Nullable DataComponent<?> fromId(int id) {
        return IDS.get(id);
    }

    public static @NotNull Collection<DataComponent<?>> values() {
        return NAMESPACES.values();
    }

    static <T> DataComponent<T> register(@NotNull String name, @Nullable BinaryTagSerializer<T> nbt) {
        DataComponent<T> impl = DataComponent.createHeadless(NAMESPACES.size(), NamespaceID.from(name), null, nbt);
        NAMESPACES.put(impl.name(), impl);
        IDS.set(impl.id(), impl);
        return impl;
    }
}
