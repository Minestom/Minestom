package fr.themode.minestom.net.packet.client.play;

import fr.themode.minestom.net.packet.PacketReader;
import fr.themode.minestom.net.packet.client.ClientPlayPacket;

public class ClientUpdateCommandBlockMinecartPacket extends ClientPlayPacket {

    public int entityId;
    public String command;
    public boolean trackOutput;

    @Override
    public void read(PacketReader reader, Runnable callback) {
        reader.readVarInt(i -> entityId = i);
        reader.readSizedString((string, length) -> command = string);
        reader.readBoolean(value -> {
            trackOutput = value;
            callback.run();
        });
    }
}
