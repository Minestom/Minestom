package fr.themode.minestom.net.packet.server.status;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;

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
