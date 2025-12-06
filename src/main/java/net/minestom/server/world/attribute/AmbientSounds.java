package net.minestom.server.world.attribute;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record AmbientSounds(
        @Nullable SoundEvent loop,
        @Nullable Mood mood,
        List<Additions> additions
) {
    public static final AmbientSounds EMPTY = new AmbientSounds(null, null, List.of());

    public static final Codec<AmbientSounds> CODEC = StructCodec.struct(
            "loop", SoundEvent.CODEC.optional(), AmbientSounds::loop,
            "mood", Mood.CODEC.optional(), AmbientSounds::mood,
            "additions", Additions.CODEC.list().optional(List.of()), AmbientSounds::additions,
            AmbientSounds::new);

    public record Mood(
            SoundEvent sound,
            int tickDelay,
            int blockSearchExtent,
            double offset
    ) {
        public static final Codec<Mood> CODEC = StructCodec.struct(
                "sound", SoundEvent.CODEC, Mood::sound,
                "tick_delay", Codec.INT, Mood::tickDelay,
                "block_search_extent", Codec.INT, Mood::blockSearchExtent,
                "offset", Codec.DOUBLE, Mood::offset,
                Mood::new);
    }

    public record Additions(SoundEvent sound, double tickChance) {
        public static final Codec<Additions> CODEC = StructCodec.struct(
                "sound", SoundEvent.CODEC, Additions::sound,
                "tick_chance", Codec.DOUBLE, Additions::tickChance,
                Additions::new);
    }
}
