package net.minestom.server.adventure.serializer.nbt;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.text.event.DataComponentValue;
import org.jetbrains.annotations.Nullable;

public sealed interface NbtDataComponentValue extends DataComponentValue permits NbtDataComponentValueImpl, NbtDataComponentValueImpl.Removed {

    static NbtDataComponentValue removed() {
        return NbtDataComponentValueImpl.Removed.INSTANCE;
    }

    static NbtDataComponentValue nbtDataComponentValue(BinaryTag tag) {
        return new NbtDataComponentValueImpl(tag);
    }

    @Nullable BinaryTag value();
}
