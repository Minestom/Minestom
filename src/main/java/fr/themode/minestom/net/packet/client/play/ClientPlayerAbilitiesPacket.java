package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientPlayerAbilitiesPacket extends ClientPlayPacket {

    public byte flags;
    public float flyingSpeed;
    public float walkingSpeed;

    @Override
    public void read(PacketReader reader) {
        this.flags = reader.readByte();
        this.flyingSpeed = reader.readFloat();
        this.walkingSpeed = reader.readFloat();
    }
}
