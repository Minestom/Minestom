package net.minestom.server.adventure;

import net.kyori.adventure.text.event.DataComponentValue;
import org.jspecify.annotations.Nullable;

sealed class MinestomDataComponentValueImpl implements MinestomDataComponentValue permits MinestomDataComponentValueImpl.Removed {
    private final Object value;

    MinestomDataComponentValueImpl(@Nullable Object value) {
        this.value = value;
    }

    @Override
    public @Nullable Object value() {
        return value;
    }

    static final class Removed extends MinestomDataComponentValueImpl implements DataComponentValue.Removed {
        static final MinestomDataComponentValue INSTANCE = new MinestomDataComponentValueImpl.Removed();

        public Removed() {
            super(null);
        }
    }

}
