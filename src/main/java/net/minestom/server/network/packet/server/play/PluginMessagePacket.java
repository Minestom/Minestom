package net.minestom.server.network.packet.server.play;

import net.minestom.server.MinecraftServer;
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

    /**
     * Get the current server brand name packet
     * <p>
     * Sent to all players when the name changes
     *
     * @return the current brand name packet
     */
    public static PluginMessagePacket getBrandPacket() {
        PluginMessagePacket brandMessage = new PluginMessagePacket();
        brandMessage.channel = "minecraft:brand";

        PacketWriter writer = new PacketWriter();
        writer.writeSizedString(MinecraftServer.getBrandName());

        brandMessage.data = writer.toByteArray();

        return brandMessage;
    }
}
