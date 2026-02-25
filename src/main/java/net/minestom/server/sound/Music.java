package net.minestom.server.sound;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;

public record Music(
        SoundEvent sound,
        int minDelay,
        int maxDelay,
        boolean replaceCurrentMusic
) {
    public static final Music MENU = new Music(SoundEvents.MUSIC_MENU, 20, 600, true);
    public static final Music CREATIVE = new Music(SoundEvents.MUSIC_CREATIVE, 12000, 24000, false);
    public static final Music CREDITS = new Music(SoundEvents.MUSIC_CREDITS, 0, 0, true);
    public static final Music END_BOSS = new Music(SoundEvents.MUSIC_DRAGON, 0, 0, true);
    public static final Music END = new Music(SoundEvents.MUSIC_END, 6000, 24000, true);
    public static final Music UNDER_WATER = new Music(SoundEvents.MUSIC_UNDER_WATER, 12000, 24000, false);
    public static final Music GAME = new Music(SoundEvents.MUSIC_GAME, 12000, 24000, false);

    public static final Codec<Music> CODEC = StructCodec.struct(
            "sound", SoundEvent.CODEC, Music::sound,
            "min_delay", Codec.INT, Music::minDelay,
            "max_delay", Codec.INT, Music::maxDelay,
            "replace_current_music", Codec.BOOLEAN, Music::replaceCurrentMusic,
            Music::new);
}
