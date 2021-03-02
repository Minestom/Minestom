package net.minestom.server.adventure;

import net.kyori.adventure.key.Key;
import net.minestom.server.sound.Sound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Adventure related utilities.
 */
public class AdventureUtils {
    private static final Map<String, Sound> SOUND_MAP =
            Arrays.stream(Sound.values()).collect(Collectors.toMap(Sound::getNamespaceID, sound -> sound));

    /**
     * Attempts to get an NMS sound from an Adventure key.
     *
     * @param name the key
     *
     * @return the sound, if found
     */
    public static @Nullable Sound asSound(@NotNull Key name) {
        return SOUND_MAP.get(name.asString());
    }
}
