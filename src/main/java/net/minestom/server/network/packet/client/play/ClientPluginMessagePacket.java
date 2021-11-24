package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ClientPluginMessagePacket(@NotNull String channel, byte[] data) implements ClientPacket {
    public ClientPluginMessagePacket {
        if (channel.length() > 256)
            throw new IllegalArgumentException("Channel cannot be more than 256 characters long");
    }

    public ClientPluginMessagePacket(BinaryReader reader) {
        this(reader.readSizedString(256), reader.readRemainingBytes());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeSizedString(channel);
        writer.writeBytes(data);
    }
}
