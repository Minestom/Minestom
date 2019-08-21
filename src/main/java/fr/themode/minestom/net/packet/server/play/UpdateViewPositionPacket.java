package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.instance.Chunk;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

public class UpdateViewPositionPacket implements ServerPacket {

    private Chunk chunk;

    public UpdateViewPositionPacket(Chunk chunk) {
        this.chunk = chunk;
    }

    @Override
    public void write(Buffer buffer) {
        Utils.writeVarInt(buffer, chunk.getChunkX());
        Utils.writeVarInt(buffer, chunk.getChunkZ());
    }

    @Override
    public int getId() {
        return 0x40;
    }
}
