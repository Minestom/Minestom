package fr.themode.minestom.net.packet.server.play;

import fr.themode.minestom.net.packet.PacketWriter;
import fr.themode.minestom.net.packet.server.ServerPacket;

public class SpawnPositionPacket implements ServerPacket {

    public int x, y, z;

    @Override
    public void write(PacketWriter writer) {
        writer.writeBlockPosition(x, y, z);
    }

    @Override
    public int getId() {
        return 0x4E;
    }
}
