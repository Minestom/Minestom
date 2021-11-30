package net.minestom.server.network.packet.server.status;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.binary.BinaryReader;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public record PongPacket(long number) implements ServerPacket {
    public PongPacket(BinaryReader reader) {
        this(reader.readLong());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeLong(number);
    }

    @Override
    public int getId() {
        return 0x01;
    }
}
