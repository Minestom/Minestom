package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.VAR_INT;

public record UpdateSimulationDistancePacket(int simulationDistance) implements ServerPacket {
    public UpdateSimulationDistancePacket(@NotNull NetworkBuffer reader) {
        this(reader.read(VAR_INT));
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(VAR_INT, simulationDistance);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SET_SIMULATION_DISTANCE;
    }
}
