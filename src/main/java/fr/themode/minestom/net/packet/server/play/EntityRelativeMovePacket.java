package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

public class EntityRelativeMovePacket implements ServerPacket {

    public int entityId;
    public short deltaX, deltaY, deltaZ;
    public boolean onGround;

    @Override
    public void write(Buffer buffer) {
        Utils.writeVarInt(buffer, entityId);
        buffer.putShort(deltaX);
        buffer.putShort(deltaY);
        buffer.putShort(deltaZ);
        buffer.putBoolean(onGround);
    }

    @Override
    public int getId() {
        return 0x28;
    }
}
