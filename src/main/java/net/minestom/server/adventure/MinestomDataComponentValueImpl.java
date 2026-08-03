package net.minestom.server.adventure;

import net.kyori.adventure.text.event.DataComponentValue;
import org.jetbrains.annotations.Nullable;

value record MinestomDataComponentValueImpl(@Nullable Object value) implements MinestomDataComponentValue {
    value record Removed() implements MinestomDataComponentValue, DataComponentValue.Removed {
        static final MinestomDataComponentValue INSTANCE = new MinestomDataComponentValueImpl.Removed();

        @Override
        public @Nullable Object value() {
            return null;
        }
    }

}
