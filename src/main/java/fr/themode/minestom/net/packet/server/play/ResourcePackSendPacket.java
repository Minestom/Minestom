package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;

public class ResourcePackSendPacket implements ServerPacket {

    public String url;
    public String hash; // Size 40

    @Override
    public void write(PacketWriter writer) {
        writer.writeSizedString(url);
        writer.writeSizedString(hash);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.RESOURCE_PACK_SEND;
    }
}
