package net.minestom.server.sound;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

record CustomSoundEvent(@NotNull Key key, @Nullable Float range) implements SoundEvent {
}
