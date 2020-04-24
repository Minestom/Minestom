package net.minestom.server.network.packet.server.status;

import net.minestom.server.network.packet.PacketWriter;
import net.minestom.server.network.packet.server.ServerPacket;

public class PongPacket implements ServerPacket {

    public long number;

    public PongPacket(long number) {
        this.number = number;
    }

    @Override
    public void write(PacketWriter writer) {
        writer.writeLong(number);
    }

    @Override
    public int getId() {
        return 0x01;
    }
}
