package net.minestom.server.raw_data;

import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public final class RawVillagerProfessionData {
    @NotNull
    public final Supplier<@Nullable SoundEvent> workSound;

    public RawVillagerProfessionData(@NotNull Supplier<@Nullable SoundEvent> workSound) {
        this.workSound = workSound;
    }

    @Nullable
    public SoundEvent getWorkSound() {
        return workSound.get();
    }
}