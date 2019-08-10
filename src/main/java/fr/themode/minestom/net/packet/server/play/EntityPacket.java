package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

public class EntityPacket implements ServerPacket {

    public int entityId;

    @Override
    public void write(Buffer buffer) {
        Utils.writeVarInt(buffer, entityId);
    }

    @Override
    public int getId() {
        return 0x2B;
    }
}
