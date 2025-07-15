package net.minestom.server.adventure.serializer.nbt;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.text.event.DataComponentValue;
import org.jspecify.annotations.Nullable;

public sealed interface NbtDataComponentValue extends DataComponentValue permits NbtDataComponentValueImpl {

    static NbtDataComponentValue removed() {
        return new NbtDataComponentValueImpl.Removed();
    }

    static NbtDataComponentValue nbtDataComponentValue(BinaryTag tag) {
        return new NbtDataComponentValueImpl(tag);
    }

    @Nullable BinaryTag value();
}
