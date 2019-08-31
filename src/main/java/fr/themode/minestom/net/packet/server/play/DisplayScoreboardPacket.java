package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;

public class DisplayScoreboardPacket implements ServerPacket {

    public byte position;
    public String scoreName;

    @Override
    public void write(PacketWriter writer) {
        writer.writeByte(position);
        writer.writeSizedString(scoreName);
    }

    @Override
    public int getId() {
        return 0x42;
    }
}
