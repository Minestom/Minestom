package net.minestom.server.sound;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

record CustomSoundEvent(@NotNull Key namespace, @Nullable Float range) implements SoundEvent {
}
