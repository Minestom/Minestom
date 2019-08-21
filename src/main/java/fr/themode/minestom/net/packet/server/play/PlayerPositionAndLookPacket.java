package fr.themode.minestom.net.packet.server.play;

import fr.adamaq01.ozao.net.Buffer;
import fr.themode.minestom.net.packet.server.ServerPacket;
import fr.themode.minestom.utils.Position;
import fr.themode.minestom.utils.Utils;

public class PlayerPositionAndLookPacket implements ServerPacket {

    public Position position;
    public byte flags;
    public int teleportId;


    @Override
    public void write(Buffer buffer) {
        buffer.putDouble(position.getX());
        buffer.putDouble(position.getY());
        buffer.putDouble(position.getZ());
        buffer.putFloat(position.getYaw());
        buffer.putFloat(position.getPitch());
        buffer.putBytes(flags);
        Utils.writeVarInt(buffer, teleportId);
    }

    @Override
    public int getId() {
        return 0x35;
    }
}