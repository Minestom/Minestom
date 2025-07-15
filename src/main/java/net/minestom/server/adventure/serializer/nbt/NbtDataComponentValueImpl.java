package net.minestom.server.adventure.serializer.nbt;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.text.event.DataComponentValue;
import org.jspecify.annotations.Nullable;

sealed class NbtDataComponentValueImpl implements NbtDataComponentValue permits NbtDataComponentValueImpl.Removed {
    private final BinaryTag tag;

    NbtDataComponentValueImpl(@Nullable BinaryTag tag) {
        this.tag = tag;
    }

    @Override
    public @Nullable BinaryTag value() {
        return tag;
    }

    static final class Removed extends NbtDataComponentValueImpl implements DataComponentValue.Removed {
        public Removed() {
            super(null);
        }
    }

}
