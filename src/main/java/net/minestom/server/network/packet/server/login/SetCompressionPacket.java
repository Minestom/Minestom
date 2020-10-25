package net.minestom.server.network.packet.server.login;

import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class SetCompressionPacket implements ServerPacket {

    public int threshold;

    public SetCompressionPacket(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeVarInt(threshold);
    }

    @Override
    public int getId() {
        return 0x03;
    }
}
