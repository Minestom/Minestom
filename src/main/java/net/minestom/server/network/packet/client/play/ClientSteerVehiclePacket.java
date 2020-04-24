package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.client.ClientPlayPacket;

public class ClientSteerVehiclePacket extends ClientPlayPacket {

    public float sideways;
    public float forward;
    public byte flags;

    @Override
    public void read(PacketReader reader) {
        this.sideways = reader.readFloat();
        this.forward = reader.readFloat();
        this.flags = reader.readByte();
    }
}
