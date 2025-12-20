package net.minestom.server.world.attribute;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.sound.Music;
import org.jetbrains.annotations.Nullable;

public record BackgroundMusic(
        @Nullable Music music,
        @Nullable Music creativeMusic,
        @Nullable Music underwaterMusic
) {
    public static final BackgroundMusic EMPTY = new BackgroundMusic(null, null, null);
    public static final BackgroundMusic OVERWORLD = new BackgroundMusic(Music.GAME, Music.CREATIVE, null);

    public static final Codec<BackgroundMusic> CODEC = StructCodec.struct(
            "music", Music.CODEC.optional(), BackgroundMusic::music,
            "creative_music", Music.CODEC.optional(), BackgroundMusic::creativeMusic,
            "underwater_music", Music.CODEC.optional(), BackgroundMusic::underwaterMusic,
            BackgroundMusic::new);
    
}
