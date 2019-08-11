package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

public class UpdateViewPositionPacket implements ServerPacket {

    private int chunkX;
    private int chunkZ;

    public UpdateViewPositionPacket(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    @Override
    public void write(Buffer buffer) {
        Utils.writeVarInt(buffer, chunkX);
        Utils.writeVarInt(buffer, chunkZ);
    }

    @Override
    public int getId() {
        return 0x40;
    }
}
