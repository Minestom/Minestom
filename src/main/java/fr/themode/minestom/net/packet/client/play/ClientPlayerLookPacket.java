package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientPlayerLookPacket extends ClientPlayPacket {

    public float yaw, pitch;
    public boolean onGround;

    @Override
    public void read(PacketReader reader, Runnable callback) {
        reader.readFloat(value -> yaw = value);
        reader.readFloat(value -> pitch = value);
        reader.readBoolean(value -> {
            onGround = value;
            callback.run();
        });
    }
}
