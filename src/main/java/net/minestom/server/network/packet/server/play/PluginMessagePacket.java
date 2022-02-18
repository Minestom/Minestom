package net.minestom.server.network.packet.server.play;

import net.minestom.server.MinecraftServer;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.ServerPacketIdentifier;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record PluginMessagePacket(String channel, byte[] data) implements ServerPacket {
    public PluginMessagePacket(BinaryReader reader) {
        this(reader.readSizedString(), reader.readRemainingBytes());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(channel);
        writer.writeBytes(data);
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
    public static @NotNull PluginMessagePacket getBrandPacket() {
        final String brandName = MinecraftServer.getBrandName();
        BinaryWriter writer = new BinaryWriter(4 + brandName.length());
        writer.writeSizedString(brandName);
        return new PluginMessagePacket("minecraft:brand", writer.toByteArray());
    }
}
