package net.minestom.server.item.component;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

public record PiercingWeapon(
        boolean dealsKnockback,
        boolean dismounts,
        @Nullable SoundEvent sound,
        @Nullable SoundEvent hitSound
) {
    public static final NetworkBuffer.Type<PiercingWeapon> NETWORK_TYPE = NetworkBufferTemplate.template(
            NetworkBuffer.BOOLEAN, PiercingWeapon::dealsKnockback,
            NetworkBuffer.BOOLEAN, PiercingWeapon::dismounts,
            SoundEvent.NETWORK_TYPE.optional(), PiercingWeapon::sound,
            SoundEvent.NETWORK_TYPE.optional(), PiercingWeapon::hitSound,
            PiercingWeapon::new);
    public static final Codec<PiercingWeapon> CODEC = StructCodec.struct(
            "deals_knockback", Codec.BOOLEAN.optional(true), PiercingWeapon::dealsKnockback,
            "dismounts", Codec.BOOLEAN.optional(false), PiercingWeapon::dismounts,
            "sound", SoundEvent.CODEC.optional(), PiercingWeapon::sound,
            "hit_sound", SoundEvent.CODEC.optional(), PiercingWeapon::hitSound,
            PiercingWeapon::new);
}
