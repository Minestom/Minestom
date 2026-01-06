package net.minestom.server.network.unsafe;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferFactory;
import net.minestom.server.registry.Registries;
import org.jetbrains.annotations.Nullable;

import java.lang.foreign.Arena;
import java.util.Objects;
import java.util.function.Supplier;

import static net.minestom.server.network.unsafe.NetworkBufferUnsafe.UNSAFE;

// Do nothing with arenas. No lifetimes are used
record NetworkBufferUnsafeFactoryImpl(@Nullable NetworkBuffer.AutoResize autoResize,
                                      @Nullable Registries registries) implements NetworkBufferFactory {

    @Override
    public NetworkBufferFactory arena(Arena arena) {
        assert false : "Arenas are unused!";
        return this;
    }

    @Override
    public NetworkBufferFactory arena(Supplier<Arena> arenaSupplier) {
        assert false : "Arenas are unused!";
        return this;
    }


    @Override
    public NetworkBufferFactory autoResize(NetworkBuffer.AutoResize autoResize) {
        Objects.requireNonNull(autoResize, "autoResize");
        return new NetworkBufferUnsafeFactoryImpl(autoResize, registries);
    }

    @Override
    public NetworkBufferFactory registry(Registries registries) {
        Objects.requireNonNull(registries, "registries");
        return new NetworkBufferUnsafeFactoryImpl(autoResize, registries);
    }

    @Override
    @SuppressWarnings("removal")
    public NetworkBuffer allocate(long initialSize) {
        final long address = UNSAFE.allocateMemory(initialSize);
        return new NetworkBufferUnsafeImpl(
                address, initialSize,
                0, 0,
                autoResize, registries);
    }
}
