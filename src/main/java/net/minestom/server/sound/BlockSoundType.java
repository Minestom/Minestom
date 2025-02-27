package net.minestom.server.sound;

import org.jetbrains.annotations.NotNull;

public interface BlockSoundType {

    float volume();

    float pitch();

    @NotNull SoundEvent breakSound();

    @NotNull SoundEvent hitSound();

    @NotNull SoundEvent fallSound();

    @NotNull SoundEvent placeSound();

    @NotNull SoundEvent stepSound();
}
