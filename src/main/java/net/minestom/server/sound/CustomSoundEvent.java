package net.minestom.server.sound;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import org.jspecify.annotations.Nullable;

record CustomSoundEvent(Key key, @Nullable Float range) implements SoundEvent {
    public static final Codec<CustomSoundEvent> CODEC = StructCodec.struct(
            "sound_id", Codec.KEY, CustomSoundEvent::key,
            "range", Codec.FLOAT.optional(), CustomSoundEvent::range,
            CustomSoundEvent::new);
}
