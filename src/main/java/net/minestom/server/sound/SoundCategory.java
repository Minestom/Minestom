package net.minestom.server.sound;

import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.sound.Sound.*;

/**
 * @deprecated Use {@link Source}
 */
@Deprecated
public enum SoundCategory {
    MASTER,
    MUSIC,
    RECORDS,
    WEATHER,
    BLOCKS,
    HOSTILE,
    NEUTRAL,
    PLAYERS,
    AMBIENT,
    VOICE;

    /**
     * Gets the Adventure source representing this sound category.
     *
     * @return the source
     */
    public @NotNull Source asSource() {
        return Source.values()[this.ordinal()];
    }
}
