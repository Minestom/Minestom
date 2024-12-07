package net.minestom.server.sound;

import net.minestom.server.utils.nbt.BinaryTagSerializer;
import net.minestom.server.utils.nbt.BinaryTagTemplate;
import org.jetbrains.annotations.NotNull;

public record Music(
        @NotNull SoundEvent sound,
        int minDelay,
        int maxDelay,
        boolean replaceCurrentMusic
) {
    public static final BinaryTagSerializer<Music> NBT_TYPE = BinaryTagTemplate.object(
            "sound", SoundEvent.NBT_TYPE, Music::sound,
            "min_delay", BinaryTagSerializer.INT, Music::minDelay,
            "max_delay", BinaryTagSerializer.INT, Music::maxDelay,
            "replace_current_music", BinaryTagSerializer.BOOLEAN, Music::replaceCurrentMusic,
            Music::new);
}
