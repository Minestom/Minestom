package net.minestom.server.adventure.serializer.nbt;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.text.event.DataComponentValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface NbtDataComponentValue extends DataComponentValue permits NbtDataComponentValueImpl {

    static @NotNull NbtDataComponentValue removed() {
        return new NbtDataComponentValueImpl.Removed();
    }

    static @NotNull NbtDataComponentValue nbtDataComponentValue(@NotNull BinaryTag tag) {
        return new NbtDataComponentValueImpl(tag);
    }

    @Nullable BinaryTag value();
}
