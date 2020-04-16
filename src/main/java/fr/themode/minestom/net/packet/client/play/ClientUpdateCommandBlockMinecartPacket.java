package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientUpdateCommandBlockMinecartPacket extends ClientPlayPacket {

    public int entityId;
    public String command;
    public boolean trackOutput;

    @Override
    public void read(PacketReader reader) {
        this.entityId = reader.readVarInt();
        this.command = reader.readSizedString();
        this.trackOutput = reader.readBoolean();
    }
}
