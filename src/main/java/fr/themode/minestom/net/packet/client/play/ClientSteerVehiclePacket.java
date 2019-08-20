package fr.themode.minestom.net.packet.client.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientSteerVehiclePacket extends ClientPlayPacket {

    public float sideways;
    public float forward;
    public byte flags;

    @Override
    public void read(Buffer buffer) {
        this.sideways = buffer.getFloat();
        this.forward = buffer.getFloat();
        this.flags = buffer.getByte();
    }
}
