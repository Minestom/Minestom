package net.minestom.server.network.packet.client.play;

import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record ClientPlayerAbilitiesPacket(byte flags) implements ClientPacket {
    public ClientPlayerAbilitiesPacket(BinaryReader reader) {
        this(reader.readByte());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeByte(flags);
    }
}
