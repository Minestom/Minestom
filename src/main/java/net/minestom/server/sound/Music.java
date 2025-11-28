package net.minestom.server.sound;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;

public record Music(
        SoundEvent sound,
        int minDelay,
        int maxDelay,
        boolean replaceCurrentMusic
) {
    public static final Codec<Music> CODEC = StructCodec.struct(
            "sound", SoundEvent.CODEC, Music::sound,
            "min_delay", Codec.INT, Music::minDelay,
            "max_delay", Codec.INT, Music::maxDelay,
            "replace_current_music", Codec.BOOLEAN, Music::replaceCurrentMusic,
            Music::new);
}
