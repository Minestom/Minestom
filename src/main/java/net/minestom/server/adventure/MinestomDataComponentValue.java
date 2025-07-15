package net.minestom.server.adventure;

import net.kyori.adventure.text.event.DataComponentValue;
import org.jspecify.annotations.Nullable;

public sealed interface MinestomDataComponentValue extends DataComponentValue permits MinestomDataComponentValueImpl {

    static MinestomDataComponentValue removed() {
        return MinestomDataComponentValueImpl.Removed.INSTANCE;
    }

    static MinestomDataComponentValue dataComponentValue(final @Nullable Object data) {
        return new MinestomDataComponentValueImpl(data);
    }

    @Nullable Object value();
}
