package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record SetPassengersPacket(int vehicleEntityId, int[] passengersId) implements ServerPacket {
    public SetPassengersPacket(BinaryReader reader) {
        this(reader.readVarInt(), reader.readVarIntArray());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(vehicleEntityId);
        writer.writeVarIntArray(passengersId);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SET_PASSENGERS;
    }
}
