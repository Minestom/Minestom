package net.minestom.server.item.component;

import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

public enum MapPostProcessing {
    LOCK,
    SCALE;
    private static final MapPostProcessing[] VALUES = values();

    public static final NetworkBuffer.Type<MapPostProcessing> NETWORK_TYPE = new NetworkBuffer.Type<>() {
        @Override
        public void write(@NotNull NetworkBuffer buffer, MapPostProcessing value) {
            buffer.write(NetworkBuffer.VAR_INT, value.ordinal());
        }

        @Override
        public MapPostProcessing read(@NotNull NetworkBuffer buffer) {
            return VALUES[buffer.read(NetworkBuffer.VAR_INT)];
        }
    };
}
