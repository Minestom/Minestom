package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;

public class KeepAlivePacket implements ServerPacket {

    private long id;

    public KeepAlivePacket(long id) {
        this.id = id;
    }

    @Override
    public void write(PacketWriter writer) {
        writer.writeLong(id);
    }

    @Override
    public int getId() {
        return 0x21;
    }
}
