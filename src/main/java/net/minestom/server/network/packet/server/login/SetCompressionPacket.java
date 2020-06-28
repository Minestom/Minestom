package net.minestom.server.network.packet.server.login;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;

public class SetCompressionPacket implements ServerPacket {

    public int threshold;

    public SetCompressionPacket(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public void write(PacketWriter writer) {
        writer.writeVarInt(threshold);
    }

    @Override
    public int getId() {
        return 0x03;
    }
}
