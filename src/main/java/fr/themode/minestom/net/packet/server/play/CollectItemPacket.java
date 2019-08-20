package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

public class CollectItemPacket implements ServerPacket {

    public int collectedEntityId;
    public int collectorEntityId;
    public int pickupItemCount;

    @Override
    public void write(Buffer buffer) {
        Utils.writeVarInt(buffer, collectedEntityId);
        Utils.writeVarInt(buffer, collectorEntityId);
        Utils.writeVarInt(buffer, pickupItemCount);
    }

    @Override
    public int getId() {
        return 0x55;
    }
}
