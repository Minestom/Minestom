package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record SetPassengersPacket(int vehicleEntityId,
                                  @NotNull List<Integer> passengersId) implements ServerPacket {
    public SetPassengersPacket {
        passengersId = List.copyOf(passengersId);
    }

    public SetPassengersPacket(BinaryReader reader) {
        this(reader.readVarInt(), reader.readVarIntList(BinaryReader::readVarInt));
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(vehicleEntityId);
        writer.writeVarIntList(passengersId, BinaryWriter::writeVarInt);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SET_PASSENGERS;
    }
}
