package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.PacketReader;
import net.minestom.server.network.packet.client.ClientPlayPacket;

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
