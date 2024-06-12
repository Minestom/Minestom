package net.minestom.server.item.enchant;

import net.minestom.server.component.DataComponent;
import net.minestom.server.network.NetworkBuffer;
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

    public static final DataComponent<Unit> DAMAGE_PROTECTION = register("damage_protection", null, null); //todo
    public static final DataComponent<Unit> DAMAGE_IMMUNITY = register("damage_immunity", null, null); //todo
    public static final DataComponent<Unit> DAMAGE = register("damage", null, null); //todo
    public static final DataComponent<Unit> SMASH_DAMAGE_PER_FALLEN_BLOCK = register("smash_damage_per_fallen_block", null, null); //todo
    public static final DataComponent<Unit> KNOCKBACK = register("knockback", null, null); //todo
    public static final DataComponent<Unit> ARMOR_EFFECTIVENESS = register("armor_effectiveness", null, null); //todo
    public static final DataComponent<Unit> POST_ATTACK = register("post_attack", null, null); //todo
    public static final DataComponent<Unit> HIT_BLOCK = register("hit_block", null, null); //todo
    public static final DataComponent<Unit> ITEM_DAMAGE = register("item_damage", null, null); //todo
    public static final DataComponent<Unit> ATTRIBUTES = register("attributes", null, null); //todo
    public static final DataComponent<Unit> EQUIPMENT_DROPS = register("equipment_drops", null, null); //todo
    public static final DataComponent<Unit> LOCATION_CHANGED = register("location_changed", null, null); //todo
    public static final DataComponent<Unit> TICK = register("tick", null, null); //todo
    public static final DataComponent<Unit> AMMO_USE = register("ammo_use", null, null); //todo
    public static final DataComponent<Unit> PROJECTILE_PIERCING = register("projectile_piercing", null, null); //todo
    public static final DataComponent<Unit> PROJECTILE_SPAWNED = register("projectile_spawned", null, null); //todo
    public static final DataComponent<Unit> PROJECTILE_SPREAD = register("projectile_spread", null, null); //todo
    public static final DataComponent<Unit> PROJECTILE_COUNT = register("projectile_count", null, null); //todo
    public static final DataComponent<Unit> TRIDENT_RETURN_ACCELERATION = register("trident_return_acceleration", null, null); //todo
    public static final DataComponent<Unit> FISHING_TIME_REDUCTION = register("fishing_time_reduction", null, null); //todo
    public static final DataComponent<Unit> FISHING_LUCK_BONUS = register("fishing_luck_bonus", null, null); //todo
    public static final DataComponent<Unit> BLOCK_EXPERIENCE = register("block_experience", null, null); //todo
    public static final DataComponent<Unit> MOB_EXPERIENCE = register("mob_experience", null, null); //todo
    public static final DataComponent<Unit> REPAIR_WITH_XP = register("repair_with_xp", null, null); //todo
    public static final DataComponent<Unit> CROSSBOW_CHARGE_TIME = register("crossbow_charge_time", null, null); //todo
    public static final DataComponent<Unit> CROSSBOW_CHARGING_SOUNDS = register("crossbow_charging_sounds", null, null); //todo
    public static final DataComponent<Unit> TRIDENT_SOUND = register("trident_sound", null, null); //todo
    public static final DataComponent<Unit> PREVENT_EQUIPMENT_DROP = register("prevent_equipment_drop", null, null); //todo
    public static final DataComponent<Unit> PREVENT_ARMOR_CHANGE = register("prevent_armor_change", null, null); //todo
    public static final DataComponent<Unit> TRIDENT_SPIN_ATTACK_STRENGTH = register("trident_spin_attack_strength", null, null); //todo


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

    static <T> DataComponent<T> register(@NotNull String name, @Nullable NetworkBuffer.Type<T> network, @Nullable BinaryTagSerializer<T> nbt) {
        DataComponent<T> impl = DataComponent.createHeadless(NAMESPACES.size(), NamespaceID.from(name), network, nbt);
        NAMESPACES.put(impl.name(), impl);
        IDS.set(impl.id(), impl);
        return impl;
    }
}
