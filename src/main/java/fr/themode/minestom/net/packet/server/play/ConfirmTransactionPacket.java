package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

public class ConfirmTransactionPacket implements ServerPacket {

    public int windowId;
    public short actionNumber;
    public boolean accepted;

    @Override
    public void write(Buffer buffer) {
        Utils.writeVarInt(buffer, windowId);
        buffer.putShort(actionNumber);
        buffer.putBoolean(accepted);
    }

    @Override
    public int getId() {
        return 0x12;
    }
}
