package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientSteerVehiclePacket extends ClientPlayPacket {

    public float sideways;
    public float forward;
    public byte flags;

    @Override
    public void read(PacketReader reader, Runnable callback) {
        reader.readFloat(value -> sideways = value);
        reader.readFloat(value -> forward = value);
        reader.readByte(value -> {
            flags = value;
            callback.run();
        });
    }
}
