package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

public class SetPassengersPacket implements ServerPacket {

    public int vehicleEntityId;
    public int[] passengersId;

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(vehicleEntityId);
        writer.writeVarIntArray(passengersId);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.SET_PASSENGERS;
    }
}
