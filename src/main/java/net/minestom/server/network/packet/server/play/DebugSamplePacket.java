package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

public record DebugSamplePacket(long @NotNull [] sample, @NotNull Type type) implements ServerPacket.Play {

    public DebugSamplePacket(@NotNull NetworkBuffer buffer) {
        this(buffer.read(NetworkBuffer.LONG_ARRAY), buffer.readEnum(Type.class));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(NetworkBuffer.LONG_ARRAY, sample);
        writer.writeEnum(Type.class, type);
    }

    public enum Type {
        TICK_TIME
    }

}
