package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.net.packet.server.ServerPacketIdentifier;

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
