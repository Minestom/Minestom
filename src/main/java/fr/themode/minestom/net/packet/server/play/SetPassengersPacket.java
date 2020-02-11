package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;

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
