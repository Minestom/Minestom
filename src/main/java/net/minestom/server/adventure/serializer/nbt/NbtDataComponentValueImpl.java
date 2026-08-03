package net.minestom.server.adventure.serializer.nbt;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.text.event.DataComponentValue;
import org.jetbrains.annotations.Nullable;

value record NbtDataComponentValueImpl(@Nullable BinaryTag value) implements NbtDataComponentValue {
    value record Removed() implements DataComponentValue.Removed, NbtDataComponentValue {
        static final NbtDataComponentValueImpl.Removed INSTANCE = new NbtDataComponentValueImpl.Removed();

        @Override
        public @Nullable BinaryTag value() {
            return null;
        }
    }
}
