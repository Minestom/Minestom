package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

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
