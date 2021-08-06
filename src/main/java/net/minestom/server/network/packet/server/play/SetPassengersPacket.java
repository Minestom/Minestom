package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class SetPassengersPacket implements ServerPacket {

    public int vehicleEntityId;
    public int[] passengersId;

    public SetPassengersPacket() {
        passengersId = new int[0];
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(vehicleEntityId);
        writer.writeVarIntArray(passengersId);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        vehicleEntityId = reader.readVarInt();
        passengersId = reader.readVarIntArray();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SET_PASSENGERS;
    }
}
