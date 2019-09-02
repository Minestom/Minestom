package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientPlayerPositionAndLookPacket extends ClientPlayPacket {

    public double x, y, z;
    public float yaw, pitch;
    public boolean onGround;

    @Override
    public void read(PacketReader reader, Runnable callback) {
        reader.readDouble(value -> x = value);
        reader.readDouble(value -> y = value);
        reader.readDouble(value -> z = value);
        reader.readFloat(value -> yaw = value);
        reader.readFloat(value -> pitch = value);
        reader.readBoolean(value -> {
            onGround = value;
            callback.run();
        });
    }
}
