package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

public class DestroyEntitiesPacket implements ServerPacket {

    public int[] entityIds;

    @Override
    public void write(Buffer buffer) {
        Utils.writeVarInt(buffer, entityIds.length);
        for (int i = 0; i < entityIds.length; i++) {
            int entityId = entityIds[i];
            Utils.writeVarInt(buffer, entityId);
        }
    }

    @Override
    public int getId() {
        return 0x37;
    }
}
