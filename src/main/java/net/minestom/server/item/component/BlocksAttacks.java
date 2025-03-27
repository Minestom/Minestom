package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.registry.ObjectSet;
import net.minestom.server.sound.SoundEvent;
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
    public static final Codec<BlocksAttacks> NBT_TYPE = StructCodec.struct(
            "block_delay_seconds", Codec.FLOAT.optional(0f), BlocksAttacks::blockDelaySeconds,
            "disable_cooldown_scale", Codec.FLOAT.optional(1f), BlocksAttacks::disableCooldownScale,
            "damage_reductions", DamageReduction.CODEC.list().optional(List.of(DamageReduction.DEFAULT)), BlocksAttacks::damageReductions,
            "item_damage", ItemDamageFunction.CODEC.optional(ItemDamageFunction.DEFAULT), BlocksAttacks::itemDamage,
            "bypassed_by", ObjectSet.<EntityType>codec(Tag.BasicType.ENTITY_TYPES).optional(), BlocksAttacks::bypassedBy,
            "block_sound", SoundEvent.CODEC.optional(), BlocksAttacks::blockSound,
            "disabled_sound", SoundEvent.CODEC.optional(), BlocksAttacks::disableSound,
            BlocksAttacks::new);

    public record ItemDamageFunction(float threshold, float base, float factor) {
        public static final ItemDamageFunction DEFAULT = new ItemDamageFunction(1f, 0f, 1f);

        public static final NetworkBuffer.Type<ItemDamageFunction> NETWORK_TYPE = NetworkBufferTemplate.template(
                NetworkBuffer.FLOAT, ItemDamageFunction::threshold,
                NetworkBuffer.FLOAT, ItemDamageFunction::base,
                NetworkBuffer.FLOAT, ItemDamageFunction::factor,
                ItemDamageFunction::new);
        public static final Codec<ItemDamageFunction> CODEC = StructCodec.struct(
                "threshold", Codec.FLOAT, ItemDamageFunction::threshold,
                "base", Codec.FLOAT, ItemDamageFunction::base,
                "factor", Codec.FLOAT, ItemDamageFunction::factor,
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
        public static final Codec<DamageReduction> CODEC = StructCodec.struct(
                "horizontal_blocking_angle", Codec.FLOAT.optional(90f), DamageReduction::horizontalBlockingAngle,
                "type", ObjectSet.<EntityType>codec(Tag.BasicType.ENTITY_TYPES).optional(), DamageReduction::type,
                "base", Codec.FLOAT, DamageReduction::base,
                "factor", Codec.FLOAT, DamageReduction::factor,
                DamageReduction::new);

    }
}
