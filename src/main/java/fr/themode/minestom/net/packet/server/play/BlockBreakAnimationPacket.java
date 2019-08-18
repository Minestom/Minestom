package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Position;
import fr.themode.minestom.utils.Utils;

public class BlockBreakAnimationPacket implements ServerPacket {

    public int entityId;
    public Position blockPosition;
    public byte destroyStage;

    @Override
    public void write(Buffer buffer) {
        Utils.writeVarInt(buffer, entityId);
        Utils.writePosition(buffer, blockPosition);
        buffer.putByte(destroyStage);
    }

    @Override
    public int getId() {
        return 0x08;
    }
}