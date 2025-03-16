package net.minestom.server.item.component;

import net.minestom.server.entity.EntityType;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.registry.ObjectSet;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.nbt.BinaryTagTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record BlocksAttacks(
        float blockDelaySeconds,
        float disableCooldownScale,
        @NotNull List<DamageReduction> damageReductions,
        @NotNull ItemDamageFunction itemDamage,
        @Nullable ObjectSet<EntityType> bypassedBy,
        @Nullable SoundEvent blockSound,
        @Nullable SoundEvent disableSound
) {
    public static final NetworkBuffer.Type<BlocksAttacks> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.FLOAT, BlocksAttacks::blockDelaySeconds,
            NetworkBuffer.FLOAT, BlocksAttacks::disableCooldownScale,
            DamageReduction.NETWORK_TYPE.list(Short.MAX_VALUE), BlocksAttacks::damageReductions,
            ItemDamageFunction.NETWORK_TYPE, BlocksAttacks::itemDamage,
            ObjectSet.networkType(Tag.BasicType.ENTITY_TYPES), BlocksAttacks::bypassedBy,
            SoundEvent.NETWORK_TYPE, BlocksAttacks::blockSound,
            SoundEvent.NETWORK_TYPE, BlocksAttacks::disableSound,
            BlocksAttacks::new);
    public static final BinaryTagSerializer<BlocksAttacks> NBT_TYPE = BinaryTagTemplate.object(
            "block_delay_seconds", BinaryTagSerializer.FLOAT.optional(0f), BlocksAttacks::blockDelaySeconds,
            "disable_cooldown_scale", BinaryTagSerializer.FLOAT.optional(1f), BlocksAttacks::disableCooldownScale,
            "damage_reductions", DamageReduction.NBT_TYPE.list().optional(List.of(DamageReduction.DEFAULT)), BlocksAttacks::damageReductions,
            "item_damage", ItemDamageFunction.NBT_TYPE.optional(ItemDamageFunction.DEFAULT), BlocksAttacks::itemDamage,
            "bypassed_by", ObjectSet.<EntityType>nbtType(Tag.BasicType.ENTITY_TYPES).optional(), BlocksAttacks::bypassedBy,
            "block_sound", SoundEvent.NBT_TYPE.optional(), BlocksAttacks::blockSound,
            "disabled_sound", SoundEvent.NBT_TYPE.optional(), BlocksAttacks::disableSound,
            BlocksAttacks::new);

    public record ItemDamageFunction(float threshold, float base, float factor) {
        public static final ItemDamageFunction DEFAULT = new ItemDamageFunction(1f, 0f, 1f);

        public static final NetworkBuffer.Type<ItemDamageFunction> NETWORK_TYPE = NetworkBufferTemplate.template(
                NetworkBuffer.FLOAT, ItemDamageFunction::threshold,
                NetworkBuffer.FLOAT, ItemDamageFunction::base,
                NetworkBuffer.FLOAT, ItemDamageFunction::factor,
                ItemDamageFunction::new);
        public static final BinaryTagSerializer<ItemDamageFunction> NBT_TYPE = BinaryTagTemplate.object(
                "threshold", BinaryTagSerializer.FLOAT, ItemDamageFunction::threshold,
                "base", BinaryTagSerializer.FLOAT, ItemDamageFunction::base,
                "factor", BinaryTagSerializer.FLOAT, ItemDamageFunction::factor,
                ItemDamageFunction::new);
    }

    public record DamageReduction(
            float horizontalBlockingAngle,
            @Nullable ObjectSet<EntityType> type,
            float base, float factor
    ) {
        public static final DamageReduction DEFAULT = new DamageReduction(90.0f, null, 0.0f, 1.0f);

        public static final NetworkBuffer.Type<DamageReduction> NETWORK_TYPE = NetworkBufferTemplate.template(
                NetworkBuffer.FLOAT, DamageReduction::horizontalBlockingAngle,
                ObjectSet.<EntityType>networkType(Tag.BasicType.ENTITY_TYPES).optional(), DamageReduction::type,
                NetworkBuffer.FLOAT, DamageReduction::base,
                NetworkBuffer.FLOAT, DamageReduction::factor,
                DamageReduction::new);
        public static final BinaryTagSerializer<DamageReduction> NBT_TYPE = BinaryTagTemplate.object(
                "horizontal_blocking_angle", BinaryTagSerializer.FLOAT.optional(90f), DamageReduction::horizontalBlockingAngle,
                "type", ObjectSet.<EntityType>nbtType(Tag.BasicType.ENTITY_TYPES).optional(), DamageReduction::type,
                "base", BinaryTagSerializer.FLOAT, DamageReduction::base,
                "factor", BinaryTagSerializer.FLOAT, DamageReduction::factor,
                DamageReduction::new);

    }
}
