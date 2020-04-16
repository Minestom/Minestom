package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

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
