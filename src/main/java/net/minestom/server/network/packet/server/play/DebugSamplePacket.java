package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;

import java.util.Arrays;

import static net.minestom.server.network.NetworkBuffer.Enum;
import static net.minestom.server.network.NetworkBuffer.LONG_ARRAY;

public record DebugSamplePacket(long[] sample, Type type) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<DebugSamplePacket> SERIALIZER = NetworkBufferTemplate.template(
            LONG_ARRAY, DebugSamplePacket::sample,
            Enum(Type.class), DebugSamplePacket::type,
            DebugSamplePacket::new);

    public enum Type {
        TICK_TIME
    }

    public DebugSamplePacket {
        sample = sample.clone();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof DebugSamplePacket(long[] sample1, Type type1))) return false;
        return type() == type1 && Arrays.equals(sample(), sample1);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(sample());
        result = 31 * result + type().hashCode();
        return result;
    }
}
