package net.minestom.server.sound;

import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;

/**
 * @deprecated Use {@link Sound.Source}
 */
@Deprecated
public enum SoundCategory implements Sound.Source.Provider {
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
     * @deprecated Use {@link #soundSource()}
     */
    @Deprecated
    public @NotNull Sound.Source asSource() {
        return this.soundSource();
    }

    @Override
    public @NotNull Sound.Source soundSource() {
        return Sound.Source.values()[this.ordinal()];
    }
}
