package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPlayPacket;
import net.minestom.server.utils.binary.BinaryBuffer;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class ClientPlayerAbilitiesPacket extends ClientPlayPacket {

    public byte flags;

    @Override
    public void read(@NotNull BinaryBuffer reader) {
        this.flags = reader.readByte();
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByte(flags);
    }
}
