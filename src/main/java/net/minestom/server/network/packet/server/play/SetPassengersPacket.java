package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryWriter;

public class SetPassengersPacket implements ServerPacket {

    public int vehicleEntityId;
    public int[] passengersId;

    @Override
    public void write(BinaryWriter writer) {
        writer.writeVarInt(vehicleEntityId);
        writer.writeVarIntArray(passengersId);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SET_PASSENGERS;
    }
}
