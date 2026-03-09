package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.NetworkBufferTemplate;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.world.clock.WorldClock;

import java.util.Map;

import static net.minestom.server.network.NetworkBuffer.*;

public record TimeUpdatePacket(long gameTime,
                               Map<RegistryKey<WorldClock>, ClockState> clocks) implements ServerPacket.Play {
    public static final NetworkBuffer.Type<TimeUpdatePacket> SERIALIZER = NetworkBufferTemplate.template(
            LONG, TimeUpdatePacket::gameTime,
            WorldClock.NETWORK_TYPE.mapValue(ClockState.NETWORK_TYPE), TimeUpdatePacket::clocks,
            TimeUpdatePacket::new);

    public TimeUpdatePacket {
        clocks = Map.copyOf(clocks);
    }

    public record ClockState(long totalTicks, boolean paused) {
        public static final NetworkBuffer.Type<ClockState> NETWORK_TYPE = NetworkBufferTemplate.template(
                VAR_LONG, ClockState::totalTicks,
                BOOLEAN, ClockState::paused,
                ClockState::new);
    }
}
