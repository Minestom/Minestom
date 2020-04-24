package net.minestom.server.network.packet.server.play;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;

public class PluginMessagePacket implements ServerPacket {

    public String channel;
    public byte[] data;

    @Override
    public void write(PacketWriter writer) {
        writer.writeSizedString(channel);
        writer.writeBytes(data);
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.PLUGIN_MESSAGE;
    }
}
