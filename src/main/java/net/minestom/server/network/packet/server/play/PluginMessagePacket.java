package net.minestom.server.network.packet.server.play;

import net.minestom.server.MinecraftServer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class PluginMessagePacket implements ServerPacket {

    public String channel;
    public byte[] data;

    public PluginMessagePacket(String channel, byte[] data) {
        this.channel = channel;
        this.data = data;
    }

    public PluginMessagePacket() {
        this("none", new byte[0]);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(channel);
        writer.writeBytes(data);
    }

    @Override
    public void read(@NotNull BinaryReader reader) {
        channel = reader.readSizedString();
        data = reader.readRemainingBytes();
    }

    @Override
    public int getId() {
        return ServerPacketIdentifier.PLUGIN_MESSAGE;
    }

    /**
     * Gets the current server brand name packet.
     * <p>
     * Sent to all players when the name changes.
     *
     * @return the current brand name packet
     */
    @NotNull
    public static PluginMessagePacket getBrandPacket() {
        PluginMessagePacket brandMessage = new PluginMessagePacket();
        brandMessage.channel = "minecraft:brand";

        final String brandName = MinecraftServer.getBrandName();
        BinaryWriter writer = new BinaryWriter(4 + brandName.length());
        writer.writeSizedString(brandName);

        brandMessage.data = writer.toByteArray();

        return brandMessage;
    }
}
