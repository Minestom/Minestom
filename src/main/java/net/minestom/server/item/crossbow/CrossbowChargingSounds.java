package net.minestom.server.item.crossbow;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.sound.SoundEvent;
import org.jspecify.annotations.Nullable;

public record CrossbowChargingSounds(
        @Nullable SoundEvent start,
        @Nullable SoundEvent mid,
        @Nullable SoundEvent end
) {
    public static final Codec<CrossbowChargingSounds> NBT_TYPE = StructCodec.struct(
            "start", SoundEvent.CODEC.optional(), CrossbowChargingSounds::start,
            "mid", SoundEvent.CODEC.optional(), CrossbowChargingSounds::mid,
            "end", SoundEvent.CODEC.optional(), CrossbowChargingSounds::end,
            CrossbowChargingSounds::new);
}
