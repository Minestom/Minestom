package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Utils;

public class SpawnPositionPacket implements ServerPacket {

    public int x, y, z;

    @Override
    public void write(Buffer buffer) {
        Utils.writePosition(buffer, x, y, z);
    }

    @Override
    public int getId() {
        return 0x4D;
    }
}
