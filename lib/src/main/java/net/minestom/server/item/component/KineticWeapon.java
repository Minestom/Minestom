package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

public record KineticWeapon(
        int contactCooldownTicks,
        int delayTicks,
        @Nullable Condition dismountConditions,
        @Nullable Condition knockbackConditions,
        @Nullable Condition damageConditions,
        float forwardMovement,
        float damageMultiplier,
        @Nullable SoundEvent sound,
        @Nullable SoundEvent hitSound
) {
    public static final NetworkBuffer.Type<KineticWeapon> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.VAR_INT, KineticWeapon::contactCooldownTicks,
            NetworkBuffer.VAR_INT, KineticWeapon::delayTicks,
            Condition.NETWORK_TYPE.optional(), KineticWeapon::dismountConditions,
            Condition.NETWORK_TYPE.optional(), KineticWeapon::knockbackConditions,
            Condition.NETWORK_TYPE.optional(), KineticWeapon::damageConditions,
            NetworkBuffer.FLOAT, KineticWeapon::forwardMovement,
            NetworkBuffer.FLOAT, KineticWeapon::damageMultiplier,
            SoundEvent.NETWORK_TYPE.optional(), KineticWeapon::sound,
            SoundEvent.NETWORK_TYPE.optional(), KineticWeapon::hitSound,
            KineticWeapon::new);
    public static final Codec<KineticWeapon> CODEC = StructCodec.struct(
            "contact_cooldown_ticks", Codec.INT.optional(10), KineticWeapon::contactCooldownTicks,
            "delay_ticks", Codec.INT.optional(0), KineticWeapon::delayTicks,
            "dismount_conditions", Condition.CODEC.optional(), KineticWeapon::dismountConditions,
            "knockback_conditions", Condition.CODEC.optional(), KineticWeapon::knockbackConditions,
            "damage_conditions", Condition.CODEC.optional(), KineticWeapon::damageConditions,
            "forward_movement", Codec.FLOAT.optional(0f), KineticWeapon::forwardMovement,
            "damage_multiplier", Codec.FLOAT.optional(1f), KineticWeapon::damageMultiplier,
            "sound", SoundEvent.CODEC.optional(), KineticWeapon::sound,
            "hit_sound", SoundEvent.CODEC.optional(), KineticWeapon::hitSound,
            KineticWeapon::new);

    public record Condition(int maxDurationTicks, float minSpeed, float minRelativeSpeed) {
        public static final NetworkBuffer.Type<Condition> NETWORK_TYPE = NetworkBufferTemplate.template(
                NetworkBuffer.VAR_INT, Condition::maxDurationTicks,
                NetworkBuffer.FLOAT, Condition::minSpeed,
                NetworkBuffer.FLOAT, Condition::minRelativeSpeed,
                Condition::new);
        public static final Codec<Condition> CODEC = StructCodec.struct(
                "max_duration_ticks", Codec.INT, Condition::maxDurationTicks,
                "min_speed", Codec.FLOAT.optional(0f), Condition::minSpeed,
                "min_relative_speed", Codec.FLOAT.optional(0f), Condition::minRelativeSpeed,
                Condition::new);
    }
}
