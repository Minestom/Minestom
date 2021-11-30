package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record UpdateSimulationDistancePacket(int simulationDistance) implements ServerPacket {
    public UpdateSimulationDistancePacket(BinaryReader reader) {
        this(reader.readVarInt());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(simulationDistance);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SET_SIMULATION_DISTANCE;
    }
}
