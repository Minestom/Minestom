package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.client.play.ClientPlayerDiggingPacket;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.BlockPosition;
import fr.themode.minestom.utils.Utils;

public class AcknowledgePlayerDiggingPacket implements ServerPacket {

    public BlockPosition blockPosition;
    public int blockStateId;
    public ClientPlayerDiggingPacket.Status status;
    public boolean successful;

    @Override
    public void write(Buffer buffer) {
        Utils.writePosition(buffer, blockPosition);
        Utils.writeVarInt(buffer, blockStateId);
        Utils.writeVarInt(buffer, status.ordinal());
        buffer.putBoolean(successful);
    }

    @Override
    public int getId() {
        return 0x5c;
    }
}
