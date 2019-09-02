package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientPlayerPositionPacket extends ClientPlayPacket {

    public double x, y, z;
    public boolean onGround;

    @Override
    public void read(PacketReader reader, Runnable callback) {
        reader.readDouble(value -> x = value);
        reader.readDouble(value -> y = value);
        reader.readDouble(value -> z = value);
        reader.readBoolean(value -> {
            onGround = value;
            callback.run();
        });
    }
}
