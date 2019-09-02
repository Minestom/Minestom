package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientPlayerAbilitiesPacket extends ClientPlayPacket {

    public byte flags;
    public float flyingSpeed;
    public float walkingSpeed;

    @Override
    public void read(PacketReader reader, Runnable callback) {
        reader.readByte(value -> flags = value);
        reader.readFloat(value -> flyingSpeed = value);
        reader.readFloat(value -> {
            walkingSpeed = value;
            callback.run();
        });
    }
}
