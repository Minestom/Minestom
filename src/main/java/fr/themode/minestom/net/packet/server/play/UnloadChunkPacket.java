package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;

public class UnloadChunkPacket implements ServerPacket {

    public int chunkX, chunkZ;

    @Override
    public void write(Buffer buffer) {
        buffer.putInt(chunkX);
        buffer.putInt(chunkZ);
    }

    @Override
    public int getId() {
        return 0x1D;
    }
}
