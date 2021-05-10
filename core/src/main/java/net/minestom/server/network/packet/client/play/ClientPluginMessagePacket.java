package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ClientPluginMessagePacket extends ClientPlayPacket {

    public String channel = "";
    public byte[] data = new byte[0];

    @Override
    public void read(@NotNull BinaryReader reader) {
        this.channel = reader.readSizedString(256);
        this.data = reader.readRemainingBytes();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        if(channel.length() > 256)
            throw new IllegalArgumentException("Channel cannot be more than 256 characters long");
        writer.writeSizedString(channel);
        writer.writeBytes(data);
    }
}
