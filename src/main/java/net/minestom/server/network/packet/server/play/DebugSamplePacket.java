package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.Enum;
import static net.minestom.server.network.NetworkBuffer.LONG_ARRAY;

public record DebugSamplePacket(long @NotNull [] sample, @NotNull Type type) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<DebugSamplePacket> SERIALIZER = NetworkBufferTemplate.template(
            LONG_ARRAY, DebugSamplePacket::sample,
            Enum(Type.class), DebugSamplePacket::type,
            DebugSamplePacket::new);

    public enum Type {
        TICK_TIME
    }
}
