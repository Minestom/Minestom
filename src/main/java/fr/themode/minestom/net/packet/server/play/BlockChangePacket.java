package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Position;
import fr.themode.minestom.utils.Utils;

public class BlockChangePacket implements ServerPacket {

    public Position position;
    public int blockId;

    @Override
    public void write(Buffer buffer) {
        Utils.writePosition(buffer, position.getX(), position.getY(), position.getZ());
        Utils.writeVarInt(buffer, blockId);
    }

    @Override
    public int getId() {
        return 0x0B;
    }
}
