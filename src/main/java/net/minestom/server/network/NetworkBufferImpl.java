package net.minestom.server.network;

import net.minestom.server.registry.Registries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

import static net.minestom.server.network.NetworkBuffer.ResizeStrategy;

final class NetworkBufferImpl {

    static final class Builder implements NetworkBuffer.Builder {
        private final int initialSize;
        private ResizeStrategy resizeStrategy;
        private Registries registries;

        public Builder(int initialSize) {
            this.initialSize = initialSize;
        }

        @Override
        public NetworkBuffer.@NotNull Builder resizeStrategy(@Nullable ResizeStrategy resizeStrategy) {
            this.resizeStrategy = resizeStrategy;
            return this;
        }

        @Override
        public NetworkBuffer.@NotNull Builder registry(Registries registries) {
            this.registries = registries;
            return this;
        }

        @Override
        public @NotNull NetworkBuffer build() {
            ByteBuffer buffer = ByteBuffer.allocateDirect(initialSize);
            return new NetworkBuffer(buffer, resizeStrategy, registries);
        }
    }
}
